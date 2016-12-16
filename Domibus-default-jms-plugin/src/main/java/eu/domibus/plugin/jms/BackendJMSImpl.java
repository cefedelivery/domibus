package eu.domibus.plugin.jms;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.NotificationType;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
import java.util.List;

import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_TYPE_SUBMIT;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class BackendJMSImpl extends AbstractBackendConnector<MapMessage, MapMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendJMSImpl.class);
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
            String errorMessage = null;
            String messageID = null;

            if (MESSAGE_TYPE_SUBMIT.equals(map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY))) {
                try {
                    messageID = this.submit(map);
                } catch (final MessagingProcessingException e) {
                    BackendJMSImpl.LOG.error("Exception occurred: ", e);
                    errorMessage = e.getMessage() + "\nError Code: " + (e.getEbms3ErrorCode() != null ? e.getEbms3ErrorCode().getErrorCodeName() : " not set");
                }
            } else {
                errorMessage = "Illegal messageType: " + map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY) +
                        "on message with JMSCorrelationId:" + map.getJMSCorrelationID() + ". Only " + MESSAGE_TYPE_SUBMIT + " messages are accepted on this queue";
                LOG.error(errorMessage);
            }
            final MessageCreator replyMessageCreator = new ReplyMessageCreator(messageID, errorMessage, map.getJMSCorrelationID());
            replyJmsTemplate.send(replyMessageCreator);

        } catch (Exception e) {
            LOG.error("Exception occurred while receiving message", e);
            throw new RuntimeException("Exception occurred while receiving message", e);
        }
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
