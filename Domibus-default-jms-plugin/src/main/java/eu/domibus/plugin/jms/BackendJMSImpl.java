package eu.domibus.plugin.jms;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.wss4j.common.crypto.CryptoService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
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
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.text.MessageFormat;
import java.util.*;

import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_ID;
import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_TYPE_SUBMIT;

/**
 * @author Christian Koch, Stefan Mueller
 */
@EnableScheduling
public class BackendJMSImpl extends AbstractBackendConnector<MapMessage, MapMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendJMSImpl.class);
    public static final String PAYLOAD_FILE_NAME = "payload.xml";
    public static final String PAYLOAD_PARAM_NAME = "payload";
    public static final String SUBMISSION_PARAM_NAME = "submissionJson";
    public static final String DOMIBUS_C4_REST_ENDPOINT = "domibus.c4.rest.endpoint";
    public static final String CERTIFICATE_PARAM_NAME = "certificate";
    public static final String AUTHENTICATION_ENDPOINT_PROPERTY_NAME = "domibus.c4.rest.authenticate.endpoint";
    public static final String PAYLOAD_ENDPOINT_PROPERTY_NAME = "domibus.c4.rest.payload.endpoint";
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

    @Autowired
    @Qualifier("nonXa")
    private DataSource dataSource;

    @Autowired
    private AccessPointHelper accessPointHelper;

    @Autowired
    private EndPointHelper endPointHelper;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    private MessageRetrievalTransformer<MapMessage> messageRetrievalTransformer;

    private MessageSubmissionTransformer<MapMessage> messageSubmissionTransformer;

    private Metric metric = new Metric();

    private org.springframework.web.client.RestTemplate restTemplate;

    private NamedParameterJdbcTemplate jdbcTemplate;

    private Map<String, String> partyAliasMap = new HashMap<>();





    @PostConstruct
    protected void init() {
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());

        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
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

    protected void authenticate(Submission submission) {
        String senderAlias;
        senderAlias = getSenderAlias(submission);
        byte[] encodedCertificate = encodeCertificate(senderAlias);
        MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
        multipartRequest.add(SUBMISSION_PARAM_NAME, submission);
        ByteArrayResource byteArrayResource = new ByteArrayResource(encodedCertificate);
        multipartRequest.add(CERTIFICATE_PARAM_NAME, byteArrayResource);
        String authenticationUrl = domibusProperties.getProperty(AUTHENTICATION_ENDPOINT_PROPERTY_NAME);
        Boolean aBoolean = restTemplate.postForObject(authenticationUrl, multipartRequest, Boolean.class);
        if (!aBoolean) {
            throw new AuthenticationException("UMDS rejected authentication");
        }
    }

    private byte[] encodeCertificate(String senderAlias) {
        try {
            Certificate certificate;
            certificate =cryptoService.getTrustStore().getCertificate(senderAlias);
            return Base64.encodeBase64(certificate.getEncoded());
        } catch (CertificateEncodingException | KeyStoreException e) {
            LOG.error(e.getMessage(), e);
            throw new AuthenticationException("Error while extracting certificate", e);
        }
    }

    private String getSenderAlias(Submission submission) {
        Submission.Party party = accessPointHelper.extractSendingAccessPoint(submission);
        String partyId = party.getPartyId();
        String alias = partyAliasMap.get(partyId);
        if (alias != null) {
            return alias;
        }
        String query = "SELECT p.NAME FROM TB_PARTY_IDENTIFIER pi ,TB_PARTY p WHERE pi.FK_PARTY=p.ID_PK AND pi.PARTY_ID=:party_id";
        SqlParameterSource namedParameters = new MapSqlParameterSource("party_id", partyId);
        try {
            alias = this.jdbcTemplate.queryForObject(query, namedParameters, String.class);
            partyAliasMap.put(partyId, alias);
            return alias;
        } catch (DataAccessException e) {
            LOG.error("No alias found for sender [{}]", partyId);
            throw new AuthenticationException("No alias found for sender");
        }
    }

    @Override
    public void deliverMessage(final String messageId) {
        metric.setStartTime(System.currentTimeMillis());
        Submission submission;
        try {
            submission = this.messageRetriever.downloadMessage(messageId);
        } catch (MessageNotFoundException e) {
            LOG.error(e.getMessage(),e);
            return;
        }
        try {
            authenticate(submission);
            sendPayload(submission);
            accessPointHelper.switchAccessPoint(submission);
            endPointHelper.switchEndPoint(submission);
            submission.setMessageId(messageIdGenerator.generateMessageId());
            messageSubmitter.submit(submission,getName());
        } catch (AuthenticationException e) {
            //return invalid message.
        } catch (MessagingProcessingException e) {
            LOG.error(e.getMessage(),e);
        }

    }

    private void sendPayload(Submission submission) {
        MultiValueMap<String, Object> multipartRequest = buildMultiPartRequestFromPayload(submission.getPayloads());
        multipartRequest.add(SUBMISSION_PARAM_NAME, submission);

        String payloadEndPointUrl = domibusProperties.getProperty(PAYLOAD_ENDPOINT_PROPERTY_NAME);

        if (LOG.isInfoEnabled()) {
            logMultipart(payloadEndPointUrl, multipartRequest);
        }

        restTemplate.postForLocation(payloadEndPointUrl, multipartRequest);
        metric.setLastChanged(System.currentTimeMillis());
    }

    private void logMultipart(String submissionRestUrl, MultiValueMap<String, Object> multipartRequest) {
        LOG.info("Sending :");
        Map<String, Object> stringObjectMap = multipartRequest.toSingleValueMap();
        Set<Map.Entry<String, Object>> entries = stringObjectMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            LOG.info("Key:[{}]", entry.getKey());
            LOG.info("Value:[{}]", entry.getValue());
        }
        LOG.info("To:[{}]", submissionRestUrl);
    }

    private MultiValueMap<String, Object> buildMultiPartRequestFromPayload(Set<Submission.Payload> payloads) {
        MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
        for (Submission.Payload payload : payloads) {
            try {
                InputStream inputStream = payload.getPayloadDatahandler().getInputStream();
                int available = inputStream.available();
                if (available == 0) {
                    LOG.warn("Payload skipped because it is empty");
                    return multipartRequest;
                }
                byte[] b = new byte[available];
                inputStream.read(b);
                ByteArrayResource byteArrayResource = new ByteArrayResource(Base64.encodeBase64(b)) {
                    @Override
                    public String getFilename() {
                        return PAYLOAD_FILE_NAME;
                    }
                };
                multipartRequest.add(PAYLOAD_PARAM_NAME, byteArrayResource);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return multipartRequest;
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
