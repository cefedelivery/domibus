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

package eu.domibus.plugin.jms;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.NotificationType;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.text.MessageFormat;
import java.util.List;

import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_ID;
import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_TYPE_SUBMIT;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class BackendJMSImpl extends AbstractBackendConnector<MapMessage, MapMessage> {

    private static final Log LOG = LogFactory.getLog(BackendJMSImpl.class);
    @Autowired
    @Qualifier(value = "replyJmsTemplate")
    private JmsOperations replyJmsTemplate;

    @Autowired
    @Qualifier(value = "mshToBackendTemplate")
    private JmsOperations mshToBackendTemplate;

    @Autowired
    @Qualifier(value = "errorNotifyConsumerTemplate")
    private JmsOperations errorNotifyConsumerTemplate;

    @Autowired
    @Qualifier(value = "errorNotifyProducerTemplate")
    private JmsOperations errorNotifyProducerTemplate;
    private MessageRetrievalTransformer<MapMessage> messageRetrievalTransformer;
    private MessageSubmissionTransformer<MapMessage> messageSubmissionTransformer;

    public BackendJMSImpl(String name) {
        super(name);
    }

    @Override
    public MessageSubmissionTransformer<MapMessage> getMessageSubmissionTransformer() {
        return this.messageSubmissionTransformer;
    }

    public void setMessageSubmissionTransformer(MessageSubmissionTransformer<MapMessage> messageSubmissionTransformer) {
        this.messageSubmissionTransformer = messageSubmissionTransformer;
    }

    @Override
    public MessageRetrievalTransformer<MapMessage> getMessageRetrievalTransformer() {
        return this.messageRetrievalTransformer;
    }

    public void setMessageRetrievalTransformer(MessageRetrievalTransformer<MapMessage> messageRetrievalTransformer) {
        this.messageRetrievalTransformer = messageRetrievalTransformer;
    }

    /**
     * This method is called when a message was received at the incoming queue
     *
     * @param map The incoming JMS Message
     */
    @JmsListener(destination = "${domibus.backend.jmsInQueue}", containerFactory = "backendJmsListenerContainerFactory")
    @Transactional
    public void receiveMessage(final MapMessage map) {
        try {
            final String messageID = map.getStringProperty(MESSAGE_ID);
            final String jmsCorrelationID = map.getJMSCorrelationID();
            final String messageType = map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY);

            LOG.info("Received message [" + messageID + "]");

            if (!MESSAGE_TYPE_SUBMIT.equals(messageType)) {
                String wrongMessageTypeMessage = getWrongMessageTypeErrorMessage(messageID, jmsCorrelationID, messageType);
                LOG.error(wrongMessageTypeMessage);
                sendReplyMessage(messageID, wrongMessageTypeMessage, jmsCorrelationID);
                return;
            }

            String errorMessage = null;
            try {
                submit(map);
            } catch (final MessagingProcessingException e) {
                LOG.error("Exception occurred receiving message [" + messageID + "]", e);
                errorMessage = e.getMessage() + "\nError Code: " + (e.getEbms3ErrorCode() != null ? e.getEbms3ErrorCode().getErrorCodeName() : " not set");
            }

            sendReplyMessage(messageID, errorMessage, jmsCorrelationID);

            LOG.info("Submitted message [" + messageID + "]");
        } catch (Exception e) {
            LOG.error("Exception occurred while receiving message", e);
            throw new RuntimeException("Exception occurred while receiving message", e);
        }
    }

    protected String getWrongMessageTypeErrorMessage(String messageID, String jmsCorrelationID, String messageType) {
        return MessageFormat.format("Illegal messageType [{0}] on message with JMSCorrelationId [{1}] and messageId [{2}]. Only [{3}] messages are accepted on this queue",
                            messageType, jmsCorrelationID, messageID, MESSAGE_TYPE_SUBMIT);
    }

    protected void sendReplyMessage(final String messageId, final String errorMessage, final String correlationId) {
        final MessageCreator replyMessageCreator = new ReplyMessageCreator(messageId, errorMessage, correlationId);
        replyJmsTemplate.send(replyMessageCreator);
    }

    @Override
    public void deliverMessage(final String messageId) {
        mshToBackendTemplate.send(new DownloadMessageCreator(messageId));
    }

    @Override
    public void messageReceiveFailed(final String messageId, final String endpoint) {
        List<ErrorResult> errors = super.getErrorsForMessage(messageId);
        errorNotifyConsumerTemplate.send(new ErrorMessageCreator(errors.get(errors.size() - 1), endpoint, NotificationType.MESSAGE_RECEIVED_FAILURE));
    }

    @Override
    public void messageSendFailed(final String messageId) {
        List<ErrorResult> errors = super.getErrorsForMessage(messageId);
        errorNotifyProducerTemplate.send(new ErrorMessageCreator(errors.get(errors.size() - 1), null, NotificationType.MESSAGE_SEND_FAILURE));
    }

    @Override
    public void messageSendSuccess(String messageId) {
        replyJmsTemplate.send(new SignalMessageCreator(messageId, NotificationType.MESSAGE_SEND_SUCCESS));
    }

    private class DownloadMessageCreator implements MessageCreator {
        private String messageId;


        public DownloadMessageCreator(final String messageId) {
            this.messageId = messageId;
        }

        @Override
        public Message createMessage(final Session session) throws JMSException {
            final MapMessage mapMessage = session.createMapMessage();
            try {
                downloadMessage(messageId, mapMessage);
            } catch (final MessageNotFoundException e) {
                throw new RuntimeException("Unable to create push message", e);
            }
            mapMessage.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, JMSMessageConstants.MESSAGE_TYPE_INCOMING);
            return mapMessage;
        }
    }
}
