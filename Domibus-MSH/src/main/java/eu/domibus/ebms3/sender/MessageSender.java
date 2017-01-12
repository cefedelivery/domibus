/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.ebms3.sender;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.DelayedDispatchMessageCreator;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import eu.domibus.messaging.MessageConstants;
import org.apache.cxf.interceptor.Fault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.soap.SOAPFaultException;


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
    JMSManager jmsManager;

    @Autowired
    @Qualifier("sendMessageQueue")
    private Queue sendMessageQueue;

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


    private void sendUserMessage(final String messageId) {
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SEND_INITIATION);
        ReliabilityChecker.CheckResult reliabilityCheckSuccessful = ReliabilityChecker.CheckResult.FAIL;
        // Assuming that everything goes fine
        ResponseHandler.CheckResult isOk = ResponseHandler.CheckResult.OK;

        LegConfiguration legConfiguration = null;
        final String pModeKey;

        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        try {
            pModeKey = pModeProvider.findPModeKeyForUserMessage(userMessage);
            LOG.debug("PMode key found : " + pModeKey);
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            LOG.info("Found leg [{}] for PMode key [{}]", legConfiguration.getName(), pModeKey);

            final SOAPMessage soapMessage = messageBuilder.buildSOAPMessage(userMessage, legConfiguration);
            final SOAPMessage response = mshDispatcher.dispatch(soapMessage, pModeKey);
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

        } catch (final EbMS3Exception e) {
            this.handleEbms3Exception(e, messageId);
        } finally {
            handleReliability(messageId, reliabilityCheckSuccessful, isOk, legConfiguration);
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

    @Transactional(propagation = Propagation.REQUIRED)
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
                jmsManager.sendMessageToQueue(new DelayedDispatchMessageCreator(messageId, message.getStringProperty(MessageConstants.ENDPOINT), delay).createMessage(), sendMessageQueue);
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
