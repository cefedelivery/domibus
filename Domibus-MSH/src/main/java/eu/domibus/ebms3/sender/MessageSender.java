package eu.domibus.ebms3.sender;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.DomibusCertificateException;
import eu.domibus.pki.PolicyService;
import org.apache.commons.lang.Validate;
import org.apache.cxf.interceptor.Fault;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import java.sql.Timestamp;
import java.util.Properties;


/**
 * This class is responsible for the handling of outgoing messages.
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
@Service(value = "messageSenderService")
public class MessageSender implements MessageListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSender.class);

    protected static String DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING ="domibus.sender.certificate.validation.onsending";
    protected static String DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING ="domibus.receiver.certificate.validation.onsending";

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
    private CertificateService certificateService;

    @Autowired
    private RetryService retryService;

    @Autowired
    PolicyService policyService;

    @Autowired
    private MessageAttemptService messageAttemptService;


    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    private void sendUserMessage(final String messageId) {
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SEND_INITIATION);

        MessageAttempt attempt = new MessageAttempt();
        attempt.setMessageId(messageId);
        attempt.setStartDate(new Timestamp(System.currentTimeMillis()));
        MessageAttemptStatus attemptStatus = MessageAttemptStatus.SUCCESS;
        String attemptError = null;


        ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.FAIL;
        // Assuming that everything goes fine
        ResponseHandler.CheckResult isOk = ResponseHandler.CheckResult.OK;

        LegConfiguration legConfiguration = null;
        final String pModeKey;

        Boolean abortSending = false;
        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        try {
            pModeKey = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING).getPmodeKey();
            LOG.debug("PMode key found : " + pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.info("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);

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

            if (!policyService.isNoSecurityPolicy(policy)) {
                if(!checkCertificatesValidity(sendingParty.getName(), receiverParty.getName())) {
                    // this flag is used in the finally clause
                    abortSending = true;
                    return;
                }
            }

            LOG.debug("PMode found : " + pModeKey);
            final SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
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
        } catch (Throwable e) {
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            throw e;
        } finally {
            if (abortSending) {
                LOG.info("Skipped checking the reliability for message [" + messageId + "]: message sending has been aborted");
                retryService.purgeTimedoutMessage(messageId);
                return;
            }
            reliabilityChecker.handleReliability(messageId, reliabilityCheckSuccessful, isOk, legConfiguration);
            try {
                attempt.setError(attemptError);
                attempt.setStatus(attemptStatus);
                attempt.setEndDate(new Timestamp(System.currentTimeMillis()));
                messageAttemptService.create(attempt);
            } catch (Exception e) {
                LOG.error("Could not create the message attempt", e);
            }
        }
    }

    protected Boolean checkCertificatesValidity(String sender, String receiver) {
        if(Boolean.parseBoolean(domibusProperties.getProperty(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING, "true"))) {
            // Verifies the validity of sender's certificate and reduces security issues due to possible hacked access points.
            try {
                if(!certificateService.isCertificateChainValid(sender)) {
                    LOG.error("Cannot send message: sender certificate is not valid or it has been revoked [" + sender + "]");
                    return false;
                }
                LOG.info("Sender certificate exists and is valid [" + sender + "]");
            } catch (DomibusCertificateException dce) {
                // Is this an error and we stop the sending or we just log a warning that we were not able to validate the cert?
                // my opinion is that since the option is enabled, we should validate no matter what => this is an error
                LOG.error("Could not verify if the certificate chain is valid for alias " + sender, dce);
                return false;
            }
        }

        if(Boolean.parseBoolean(domibusProperties.getProperty(DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING, "true"))) {
            // Verifies the validity of receiver's certificate and reduces security issues due to possible hacked access points.
            try {
                if (!certificateService.isCertificateChainValid(receiver)) {
                    LOG.error("Cannot send message: receiver certificate is not valid or it has been revoked [" + receiver + "]");
                    return false;
                }
            } catch (DomibusCertificateException dce) {
                // Is this an error and we stop the sending or we just log a warning that we were not able to validate the cert?
                // my opinion is that since the option is enabled, we should validate no matter what => this is an error
                LOG.error("Could not verify if the certificate chain is valid for alias " + receiver, dce);
                return false;
            }
        }
        return true;
    }

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
        sendUserMessage(messageId);
    }

}
