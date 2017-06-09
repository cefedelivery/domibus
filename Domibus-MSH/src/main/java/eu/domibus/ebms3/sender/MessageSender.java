package eu.domibus.ebms3.sender;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.message.attempt.MessageAttempt;
import eu.domibus.api.message.attempt.MessageAttemptService;
import eu.domibus.api.message.attempt.MessageAttemptStatus;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;
import java.sql.Timestamp;


/**
 * This class is responsible for the handling of outgoing messages.
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
@Service(value = "messageSenderService")
public class MessageSender implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSender.class);

    private final String UNRECOVERABLE_ERROR_RETRY = "domibus.dispatch.ebms.error.unrecoverable.retry";

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    private ErrorLogDao errorLogDao;

    @Autowired
    private MessagingDao messagingDao;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

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
    private BackendNotificationService backendNotificationService;

    @Autowired
    private UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    CertificateService certificateService;

    @Autowired
    RetryService retryService;

    @Autowired
    PolicyService policyService;

    @Autowired
    private MessageAttemptService messageAttemptService;


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
            pModeKey = pModeProvider.findPModeKeyForUserMessage(userMessage, MSHRole.SENDING);
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
                // Verifies the validity of sender's certificate and reduces security issues due to possible hacked access points.
                try {
                    certificateService.isCertificateValid(sendingParty.getName());
                    LOG.info("Sender certificate exists and is valid [" + sendingParty.getName() + "]");
                } catch (DomibusCertificateException dcEx) {
                    String msg = "Could not find and verify sender's certificate [" + sendingParty.getName() + "]";
                    LOG.error(msg, dcEx);
                    EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0101, msg, null, dcEx);
                    ex.setMshRole(MSHRole.SENDING);
                    throw ex;
                }

                if (certificateService.isCertificateValidationEnabled()) {
                    try {
                        boolean certificateChainValid = certificateService.isCertificateChainValid(receiverParty.getName());
                        if (!certificateChainValid) {
                            LOG.error("Cannot send message: receiver certificate is not valid or it has been revoked [" + receiverParty.getName() + "]");
                            retryService.purgeTimedoutMessage(messageId);
                            abortSending = true;
                            return;
                        }
                    } catch (Exception e) {
                        LOG.warn("Could not verify if the certificate chain is valid for alias " + receiverParty.getName(), e);
                    }
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
                this.handleEbms3Exception((EbMS3Exception) soapFEx.getCause().getCause(), messageId);
            } else {
                LOG.warn("Error for message with ID [" + messageId + "]", soapFEx);
            }
            attemptError = soapFEx.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
        } catch (final EbMS3Exception e) {
            this.handleEbms3Exception(e, messageId);
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
        } catch (Throwable e) {
            attemptError = e.getMessage();
            attemptStatus = MessageAttemptStatus.ERROR;
            throw e;
        } finally {
            if (abortSending) {
                LOG.debug("Skipped checking the reliability for message [" + messageId + "]: message sending has been aborted");
                return;
            }
            handleReliability(messageId, reliabilityCheckSuccessful, isOk, legConfiguration);
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

    private void handleReliability(String messageId, ReliabilityChecker.CheckResult reliabilityCheckSuccessful, ResponseHandler.CheckResult isOk, LegConfiguration legConfiguration) {
        switch (reliabilityCheckSuccessful) {
            case OK:
                switch (isOk) {
                    case OK:
                        userMessageLogDao.setMessageAsAcknowledged(messageId);
                        break;
                    case WARNING:
                        userMessageLogDao.setMessageAsAckWithWarnings(messageId);
                        break;
                    default:
                        assert false;
                }
                backendNotificationService.notifyOfSendSuccess(messageId);
                userMessageLogDao.setAsNotified(messageId);
                messagingDao.clearPayloadData(messageId);
                LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SEND_SUCCESS);
                break;
            case WAITING_FOR_CALLBACK:
                userMessageLogDao.setMessageAsWaitingForReceipt(messageId);
                break;
            case FAIL:
                updateRetryLoggingService.updateRetryLogging(messageId, legConfiguration);
        }
    }

    /**
     * This method is responsible for the ebMS3 error handling (creation of errorlogs and marking message as sent)
     *
     * @param exceptionToHandle the exception {@link eu.domibus.common.exception.EbMS3Exception} that needs to be handled
     * @param messageId         id of the message the exception belongs to
     */
    private void handleEbms3Exception(final EbMS3Exception exceptionToHandle, final String messageId) {
        exceptionToHandle.setRefToMessageId(messageId);
        if (!exceptionToHandle.isRecoverable() && !Boolean.parseBoolean(System.getProperty(UNRECOVERABLE_ERROR_RETRY))) {
            userMessageLogDao.setMessageAsAcknowledged(messageId);
            // TODO Shouldn't clear the payload data here ?
        }

        exceptionToHandle.setMshRole(MSHRole.SENDING);
        LOG.error("Error sending message with ID [" + messageId + "]", exceptionToHandle);
        this.errorLogDao.create(new ErrorLogEntry(exceptionToHandle));
        // The backends are notified that an error occurred in the UpdateRetryLoggingService
    }

    @Transactional(propagation = Propagation.REQUIRED, timeout = 300)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void onMessage(final Message message) {
        LOG.debug("Processing message [{}]", message);
        Long delay = null;
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
