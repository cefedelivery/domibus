package eu.domibus.ebms3.sender;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.api.metrics.Metrics;
import eu.domibus.api.security.ChainCertificateInvalidException;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.ReliabilityService;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.pki.PolicyService;
import org.apache.commons.lang.Validate;
import org.apache.cxf.interceptor.Fault;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.Set;

import static com.codahale.metrics.MetricRegistry.name;


/**
 * This class is responsible for the handling of outgoing messages.
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
@Service(value = "messageSenderService")
public class MessageSender implements MessageListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSender.class);

    private static final Set<MessageStatus> ALLOWED_STATUSES_FOR_SENDING = EnumSet.of(MessageStatus.SEND_ENQUEUED, MessageStatus.WAITING_FOR_RETRY);

    @Autowired
    private UserMessageService userMessageService;

    @Autowired
    private MessagingDao messagingDao;


    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private MSHDispatcher mshDispatcher;

    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Autowired
    private ReliabilityChecker reliabilityChecker;

    @Autowired
    private ResponseHandler responseHandler;

    @Autowired
    private RetryService retryService;

    @Autowired
    private MessageAttemptService messageAttemptService;

    @Autowired
    private MessageExchangeService messageExchangeService;

    @Autowired
    PolicyService policyService;

    @Autowired
    private ReliabilityService reliabilityService;

    @Autowired
    UserMessageLogDao userMessageLogDao;


    private final Counter pendingRequests = Metrics.METRIC_REGISTRY.counter(name(MessageSender.class, "senderCounter"));

    private void sendUserMessage(final String messageId) {
        final MessageStatus messageStatus = userMessageLogDao.getMessageStatus(messageId);
        if (!ALLOWED_STATUSES_FOR_SENDING.contains(messageStatus)) {
            LOG.warn("Message [{}] has a status [{}] which is not allowed for sending. Only the statuses [{}] are allowed", messageId, messageStatus, ALLOWED_STATUSES_FOR_SENDING);
            return;
        }
        final Timer.Context context = Metrics.METRIC_REGISTRY.timer(name(MessageSender.class, "onMessage")).time();
        pendingRequests.inc();


        LOG.businessDebug(DomibusMessageCode.BUS_MESSAGE_SEND_INITIATION);

        MessageAttempt attempt = new MessageAttempt();
        attempt.setMessageId(messageId);
        attempt.setStartDate(new Timestamp(System.currentTimeMillis()));
        MessageAttemptStatus attemptStatus = MessageAttemptStatus.SUCCESS;
        String attemptError = null;


        ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.SEND_FAIL;
        // Assuming that everything goes fine
        ResponseHandler.CheckResult isOk = ResponseHandler.CheckResult.OK;

        LegConfiguration legConfiguration = null;
        final String pModeKey;

        Boolean abortSending = false;
        final Timer.Context findUserMessage = Metrics.METRIC_REGISTRY.timer(name(MessageSender.class, "findUserMessage")).time();
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        findUserMessage.stop();
        try {
            final Timer.Context legConfigContext = Metrics.METRIC_REGISTRY.timer(name(MessageSender.class, "legConfigContext")).time();
            pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            LOG.debug("PMode key found : " + pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            legConfigContext.stop();
            LOG.debug("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);

            Policy policy;
            try {
                policy = policyService.parsePolicy("policies/" + legConfiguration.getSecurity().getPolicy());
            } catch (final ConfigurationException e) {

                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Policy configuration invalid", null, e);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }

            Party sendingParty = pModeProvider.getSenderParty(pModeKey);
            Validate.notNull(sendingParty, "Initiator party was not found");
            Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
            Validate.notNull(receiverParty, "Responder party was not found");

            try {
                messageExchangeService.verifyReceiverCertificate(legConfiguration, receiverParty.getName());
                messageExchangeService.verifySenderCertificate(legConfiguration, sendingParty.getName());
            } catch (ChainCertificateInvalidException cciEx) {
                LOG.securityError(DomibusMessageCode.SEC_INVALID_X509CERTIFICATE, cciEx, null);
                attemptError = cciEx.getMessage();
                attemptStatus = MessageAttemptStatus.ABORT;
                // this flag is used in the finally clause
                abortSending = true;
                return;
            }

            LOG.debug("PMode found : " + pModeKey);
            final Timer.Context buildSoapMessageContext = Metrics.METRIC_REGISTRY.timer(name(MessageSender.class, "messageBuilder.buildSOAPMessage(userMessage, legConfiguration)")).time();
            final SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
            buildSoapMessageContext.stop();
            final SOAPMessage response = mshDispatcher.dispatch(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
            isOk = responseHandler.handle(response);
            if (ResponseHandler.CheckResult.UNMARSHALL_ERROR.equals(isOk)) {
                EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "Problem occurred during marshalling", messageId, null);
                e.setMshRole(MSHRole.SENDING);
                throw e;
            }
            reliabilityCheckSuccessful = reliabilityChecker.check(soapMessage, response, pModeKey);
        } catch (final SOAPFaultException soapFEx) {
            if (soapFEx.getCause() instanceof Fault && soapFEx.getCause().getCause() instanceof EbMS3Exception) {
                reliabilityChecker.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), messageId);
            } else {
                LOG.warn("Error for message with ID [" + messageId + "]", soapFEx);
            }
            attemptError = soapFEx.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
        } catch (final EbMS3Exception e) {
            reliabilityChecker.handleEbms3Exception(e, messageId);
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
        } catch (Throwable t) {
            //NOSONAR: Catching Throwable is done on purpose in order to even catch out of memory exceptions in case large files are sent.
            LOG.error("Error sending message [{}]", messageId, t);
            attemptError = t.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            throw t;
        } finally {
            try {
                if (abortSending) {
                    LOG.debug("Skipped checking the reliability for message [" + messageId + "]: message sending has been aborted");
                    retryService.purgeTimedoutMessageInANewTransaction(messageId);
                } else {
                    final Timer.Context reliabilityContext = Metrics.METRIC_REGISTRY.timer(name(MessageSender.class, "handleReliability")).time();
                    reliabilityService.handleReliability(messageId, reliabilityCheckSuccessful, isOk, legConfiguration);
                    reliabilityContext.stop();
                    LOG.info("Sending message to [{}] with messageId [{}]",
                            ((PartyId) userMessage.getPartyInfo().getTo().getPartyId().toArray()[0]).getValue(),
                            userMessage.getMessageInfo().getMessageId());
                }
                final Timer.Context attemptContext = Metrics.METRIC_REGISTRY.timer(name(MessageSender.class, "attempt")).time();
                attempt.setError(attemptError);
                attempt.setStatus(attemptStatus);
                attempt.setEndDate(new Timestamp(System.currentTimeMillis()));
                messageAttemptService.create(attempt);
                attemptContext.stop();
            } catch (Exception ex) {
                LOG.error("Finally: ", ex);
            } finally {
                context.stop();
                pendingRequests.dec();
            }
        }
    }

    private static final Meter requestsPerSecond = Metrics.METRIC_REGISTRY.meter(name(MessageSender.class, "senderMeter"));


    @Transactional(propagation = Propagation.REQUIRED, timeout = 300)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void onMessage(final Message message) {
        LOG.debug("Processing message [{}]", message);
        Long delay;
        String messageId = null;
        try {
            messageId = message.getStringProperty(MessageConstants.MESSAGE_ID);
            LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, messageId);
            delay = message.getLongProperty(MessageConstants.DELAY);
            if (delay > 0) {
                userMessageService.scheduleSending(messageId, delay);
                return;
            }
        } catch (final NumberFormatException nfe) {
            //This is ok, no delay has been set
        } catch (final JMSException e) {
            LOG.error("Error processing message", e);
        }
        requestsPerSecond.mark();
        sendUserMessage(messageId);
    }

}
