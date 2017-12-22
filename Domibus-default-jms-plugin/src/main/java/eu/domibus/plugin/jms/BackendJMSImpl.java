package eu.domibus.plugin.jms;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_ID;
import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_TYPE_SUBMIT;

/**
 * @author Christian Koch, Stefan Mueller
 */
@EnableScheduling
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

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    private MessageRetrievalTransformer<MapMessage> messageRetrievalTransformer;

    private MessageSubmissionTransformer<MapMessage> messageSubmissionTransformer;

    private Metric metric = new Metric();


    private org.springframework.web.client.RestTemplate restTemplate;

    @PostConstruct
    protected void init() {
        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>(
                Arrays.asList(new MappingJackson2HttpMessageConverter(), new ResourceHttpMessageConverter()));
        restTemplate = new RestTemplate(converters);
    }

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
    //Propagation.REQUIRES_NEW is needed in order to avoid sending the JMS message before the database data is commited; probably this is a bug in Atomikos which will be solved by performing an upgrade
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void receiveMessage(final MapMessage map) {
        try {
            String messageID = map.getStringProperty(MESSAGE_ID);
            final String jmsCorrelationID = map.getJMSCorrelationID();
            final String messageType = map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY);

            LOG.info("Received message with messageId [" + messageID + "], jmsCorrelationID [" + jmsCorrelationID + "]");

            if (!MESSAGE_TYPE_SUBMIT.equals(messageType)) {
                String wrongMessageTypeMessage = getWrongMessageTypeErrorMessage(messageID, jmsCorrelationID, messageType);
                LOG.error(wrongMessageTypeMessage);
                sendReplyMessage(messageID, wrongMessageTypeMessage, jmsCorrelationID);
                return;
            }

            String errorMessage = null;
            try {
                //in case the messageID is not sent by the user it will be generated
                messageID = submit(map);
            } catch (final MessagingProcessingException e) {
                LOG.error("Exception occurred receiving message [" + messageID + "], jmsCorrelationID [" + jmsCorrelationID + "]", e);
                errorMessage = e.getMessage() + ": Error Code: " + (e.getEbms3ErrorCode() != null ? e.getEbms3ErrorCode().getErrorCodeName() : " not set");
            }

            sendReplyMessage(messageID, errorMessage, jmsCorrelationID);

            LOG.info("Submitted message with messageId [" + messageID + "], jmsCorrelationID [" + jmsCorrelationID + "]");
        } catch (Exception e) {
            LOG.error("Exception occurred while receiving message [" + map + "]", e);
            throw new DefaultJmsPluginException("Exception occurred while receiving message [" + map + "]", e);
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
        metric.setStartTime(System.currentTimeMillis());
        try {
            Submission submission = this.messageRetriever.downloadMessage(messageId);
            String submissionRestUrl = domibusProperties.getProperty("domibus.c4.rest.endpoint");
            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

            // creating an HttpEntity for the JSON part
            HttpHeaders jsonHeader = new HttpHeaders();
            jsonHeader.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Submission> jsonHttpEntity = new HttpEntity<>(submission, jsonHeader);

            // creating an HttpEntity for the binary part
            HttpHeaders pictureHeader = new HttpHeaders();
            pictureHeader.setContentType(MediaType.IMAGE_PNG);
            submission.getPayloads().
            HttpEntity<ByteArrayResource> picturePart = new HttpEntity<>(pngPicture, pictureHeader);

            restTemplate.postForObject(submissionRestUrl, submission, Submission.class);
            /*Set<Submission.Payload> payloads = submission.getPayloads();
            for (Submission.Payload payload : payloads) {
                response.addPayload(payload);
            }*/
            // this.messageSubmitter.submit(response, this.getName());
            //long currentTimeMillis = System.currentTimeMillis();
            metric.setLastChanged(System.currentTimeMillis());
        } catch (MessageNotFoundException e) {
            LOG.error("Error downloading message " + e);
        } catch (MessagingProcessingException e) {
            LOG.error("Error while processing submission ", e);
        }
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        errorNotifyConsumerTemplate.send(
                new ErrorMessageCreator(messageReceiveFailureEvent.getErrorResult(),
                        messageReceiveFailureEvent.getEndpoint(),
                        NotificationType.MESSAGE_RECEIVED_FAILURE)
        );
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
                throw new DefaultJmsPluginException("Unable to create push message", e);
            }
            mapMessage.setStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY, JMSMessageConstants.MESSAGE_TYPE_INCOMING);
            return mapMessage;
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void doSomething() {
        if (metric.ofInterest()) {
            LOG.info("METRICS:[{}] messages send and return from c3 to c4 in [{}] seconds", metric.getCounter(), (double) (metric.lastChanged - metric.getStartTime()) / 1000);
            metric.clear();
        }
    }

    static private class Metric {
        Long startTime;
        Long lastChanged;
        int counter = 0;

        public boolean ofInterest() {
            return lastChanged != null && lastChanged < (System.currentTimeMillis() + 10000);
        }

        public void clear() {
            startTime = null;
            lastChanged = null;
            counter = 0;
        }

        public Long getStartTime() {
            return startTime;
        }

        public Long getLastChanged() {
            return lastChanged;
        }

        public void setStartTime(Long startTime) {
            if (this.startTime == null) {
                this.startTime = startTime;
            }
        }

        public void setLastChanged(Long lastChanged) {
            this.lastChanged = lastChanged;
            counter++;
        }

        public int getCounter() {
            return counter;
        }
    }
}
