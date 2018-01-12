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

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.text.MessageFormat;
import java.util.*;

import static eu.domibus.ebms3.common.model.Property.MIME_TYPE;
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
    public static final String CERTIFICATE_PARAM_NAME = "certificate";
    public static final String AUTHENTICATION_ENDPOINT_PROPERTY_NAME = "domibus.c4.rest.authenticate.endpoint";
    public static final String PAYLOAD_ENDPOINT_PROPERTY_NAME = "domibus.c4.rest.payload.endpoint";
    public static final String CID_MESSAGE = "cid:message";
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

    private final static String HAPPY_FLOW_MESSAGE_TEMPLATE = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<message_id>\n" +
            " $messId\n" +
            "</message_id>\n" +
            "<dataset>\n" +
            "<record><id>1</id><first_name>Belita</first_name><last_name>MacMeanma</last_name><email>bmacmeanma0@alexa.com</email><gender>Female</gender><ip_address>211.210.105.141</ip_address></record><record><id>2</id><first_name>Delainey</first_name><last_name>Sarll</last_name><email>dsarll1@xinhuanet.com</email><gender>Male</gender><ip_address>172.215.113.41</ip_address></record><record><id>3</id><first_name>Rafaela</first_name><last_name>Jandel</last_name><email>rjandel2@usda.gov</email><gender>Female</gender><ip_address>176.76.130.69</ip_address></record><record><id>4</id><first_name>Fredrika</first_name><last_name>Dunbabin</last_name><email>fdunbabin3@google.com.br</email><gender>Female</gender><ip_address>28.5.174.234</ip_address></record><record><id>5</id><first_name>Othilie</first_name><last_name>Braniff</last_name><email>obraniff4@redcross.org</email><gender>Female</gender><ip_address>135.122.142.137</ip_address></record><record><id>6</id><first_name>Filmer</first_name><last_name>Wands</last_name><email>fwands5@newsvine.com</email><gender>Male</gender><ip_address>245.77.82.100</ip_address></record><record><id>7</id><first_name>Bernie</first_name><last_name>Le feaver</last_name><email>blefeaver6@usda.gov</email><gender>Male</gender><ip_address>142.226.208.76</ip_address></record><record><id>8</id><first_name>Dacy</first_name><last_name>Di Antonio</last_name><email>ddiantonio7@bbb.org</email><gender>Female</gender><ip_address>235.214.118.96</ip_address></record><record><id>9</id><first_name>Hobey</first_name><last_name>Di Pietro</last_name><email>hdipietro8@nps.gov</email><gender>Male</gender><ip_address>166.30.27.83</ip_address></record><record><id>10</id><first_name>Catha</first_name><last_name>Denkel</last_name><email>cdenkel9@princeton.edu</email><gender>Female</gender><ip_address>102.60.69.38</ip_address></record><record><id>11</id><first_name>Jeralee</first_name><last_name>Gorling</last_name><email>jgorlinga@google.ca</email><gender>Female</gender><ip_address>217.169.183.180</ip_address></record><record><id>12</id><first_name>Henrietta</first_name><last_name>Aloshechkin</last_name><email>haloshechkinb@umich.edu</email><gender>Female</gender><ip_address>128.2.221.166</ip_address></record><record><id>13</id><first_name>Georges</first_name><last_name>Veregan</last_name><email>gvereganc@seattletimes.com</email><gender>Male</gender><ip_address>117.64.187.183</ip_address></record><record><id>14</id><first_name>Dara</first_name><last_name>Shottin</last_name><email>dshottind@weather.com</email><gender>Female</gender><ip_address>167.185.3.185</ip_address></record><record><id>15</id><first_name>Jerry</first_name><last_name>Attrill</last_name><email>jattrille@nps.gov</email><gender>Male</gender><ip_address>144.46.79.18</ip_address></record><record><id>16</id><first_name>Worth</first_name><last_name>Louche</last_name><email>wlouchef@vkontakte.ru</email><gender>Male</gender><ip_address>17.117.2.116</ip_address></record><record><id>17</id><first_name>Gabie</first_name><last_name>Fontel</last_name><email>gfontelg@nbcnews.com</email><gender>Female</gender><ip_address>94.216.217.36</ip_address></record><record><id>18</id><first_name>Stanton</first_name><last_name>Millott</last_name><email>smillotth@google.nl</email><gender>Male</gender><ip_address>6.194.119.179</ip_address></record><record><id>19</id><first_name>Hedi</first_name><last_name>Pele</last_name><email>hpelei@jiathis.com</email><gender>Female</gender><ip_address>198.140.7.33</ip_address></record><record><id>20</id><first_name>Nils</first_name><last_name>Klesl</last_name><email>nkleslj@woothemes.com</email><gender>Male</gender><ip_address>106.74.129.90</ip_address></record><record><id>21</id><first_name>Bucky</first_name><last_name>Hobbema</last_name><email>bhobbemak@livejournal.com</email><gender>Male</gender><ip_address>173.139.210.39</ip_address></record><record><id>22</id><first_name>Araldo</first_name><last_name>Claye</last_name><email>aclayel@elpais.com</email><gender>Male</gender><ip_address>116.15.8.224</ip_address></record><record><id>23</id><first_name>Jules</first_name><last_name>Heninghem</last_name><email>jheninghemm@biblegateway.com</email><gender>Male</gender><ip_address>196.24.132.34</ip_address></record><record><id>24</id><first_name>Trista</first_name><last_name>Kiloh</last_name><email>tkilohn@npr.org</email><gender>Female</gender><ip_address>108.148.209.172</ip_address></record><record><id>25</id><first_name>Clevie</first_name><last_name>Drinkall</last_name><email>cdrinkallo@blogtalkradio.com</email><gender>Male</gender><ip_address>63.122.167.93</ip_address></record><record><id>26</id><first_name>Monte</first_name><last_name>Deary</last_name><email>mdearyp@fc2.com</email><gender>Male</gender><ip_address>170.13.123.223</ip_address></record><record><id>27</id><first_name>Teresina</first_name><last_name>Keuning</last_name><email>tkeuningq@ask.com</email><gender>Female</gender><ip_address>29.193.166.64</ip_address></record><record><id>28</id><first_name>Noam</first_name><last_name>Muckley</last_name><email>nmuckleyr@cbc.ca</email><gender>Male</gender><ip_address>246.237.66.187</ip_address></record><record><id>29</id><first_name>Cordelia</first_name><last_name>Bussens</last_name><email>cbussenss@artisteer.com</email><gender>Female</gender><ip_address>102.234.75.160</ip_address></record><record><id>30</id><first_name>Henrik</first_name><last_name>Paffley</last_name><email>hpaffleyt@upenn.edu</email><gender>Male</gender><ip_address>246.79.215.136</ip_address></record><record><id>31</id><first_name>Branden</first_name><last_name>Stannett</last_name><email>bstannettu@yahoo.com</email><gender>Male</gender><ip_address>161.122.87.149</ip_address></record><record><id>32</id><first_name>Madelle</first_name><last_name>Drayton</last_name><email>mdraytonv@tmall.com</email><gender>Female</gender><ip_address>69.170.17.15</ip_address></record><record><id>33</id><first_name>Flemming</first_name><last_name>Hastie</last_name><email>fhastiew@statcounter.com</email><gender>Male</gender><ip_address>194.30.236.45</ip_address></record><record><id>34</id><first_name>Torrance</first_name><last_name>Mielnik</last_name><email>tmielnikx@home.pl</email><gender>Male</gender><ip_address>130.163.101.62</ip_address></record><record><id>35</id><first_name>Cinnamon</first_name><last_name>Trevor</last_name><email>ctrevory@boston.com</email><gender>Female</gender><ip_address>132.206.141.48</ip_address></record><record><id>36</id><first_name>Deanne</first_name><last_name>Gullen</last_name><email>dgullenz@rambler.ru</email><gender>Female</gender><ip_address>134.61.119.145</ip_address></record><record><id>37</id><first_name>Wyatan</first_name><last_name>Rudgard</last_name><email>wrudgard10@addthis.com</email><gender>Male</gender><ip_address>119.131.19.119</ip_address></record><record><id>38</id><first_name>Thomasa</first_name><last_name>Keme</last_name><email>tkeme11@storify.com</email><gender>Female</gender><ip_address>29.51.65.34</ip_address></record><record><id>39</id><first_name>Mead</first_name><last_name>Cobain</last_name><email>mcobain12@youtu.be</email><gender>Female</gender><ip_address>177.138.6.69</ip_address></record><record><id>40</id><first_name>Baillie</first_name><last_name>Sommerlie</last_name><email>bsommerlie13@home.pl</email><gender>Male</gender><ip_address>46.91.193.197</ip_address></record><record><id>41</id><first_name>Cindi</first_name><last_name>Waldocke</last_name><email>cwaldocke14@nature.com</email><gender>Female</gender><ip_address>211.123.179.43</ip_address></record><record><id>42</id><first_name>Sophie</first_name><last_name>Weddell</last_name><email>sweddell15@tiny.cc</email><gender>Female</gender><ip_address>92.79.6.93</ip_address></record><record><id>43</id><first_name>Faydra</first_name><last_name>Spata</last_name><email>fspata16@bloomberg.com</email><gender>Female</gender><ip_address>3.85.1.239</ip_address></record><record><id>44</id><first_name>Monte</first_name><last_name>Philipeau</last_name><email>mphilipeau17@examiner.com</email><gender>Male</gender><ip_address>49.233.30.244</ip_address></record><record><id>45</id><first_name>Garrott</first_name><last_name>Creer</last_name><email>gcreer18@webnode.com</email><gender>Male</gender><ip_address>253.166.143.212</ip_address></record><record><id>46</id><first_name>Harp</first_name><last_name>Wherrett</last_name><email>hwherrett19@squarespace.com</email><gender>Male</gender><ip_address>197.232.85.3</ip_address></record><record><id>47</id><first_name>Miller</first_name><last_name>Wilsee</last_name><email>mwilsee1a@wix.com</email><gender>Male</gender><ip_address>242.106.77.87</ip_address></record><record><id>48</id><first_name>Prentiss</first_name><last_name>Tucknott</last_name><email>ptucknott1b@wix.com</email><gender>Male</gender><ip_address>107.41.137.99</ip_address></record><record><id>49</id><first_name>Muffin</first_name><last_name>Mulkerrins</last_name><email>mmulkerrins1c@cisco.com</email><gender>Female</gender><ip_address>219.94.140.169</ip_address></record><record><id>50</id><first_name>Tamera</first_name><last_name>Skade</last_name><email>tskade1d@flavors.me</email><gender>Female</gender><ip_address>140.28.170.139</ip_address></record>\n" +
            "</dataset>";

    private final static String UMDS_REJECTED_TEMPLATE = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<message_id>\n" +
            " $messId\n" +
            "</message_id>\n" +
            "<error>UMDS has rejected message</error>";


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

            LOG.debug("Submitted message with messageId [" + messageID + "], jmsCorrelationID [" + jmsCorrelationID + "]");
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
            LOG.error("UMDS rejected authentication for message with id[{}]",submission.getMessageId());
            throw new AuthenticationException("UMDS rejected authentication");
        }
    }

    private byte[] encodeCertificate(String senderAlias) {
        try {
            Certificate certificate;
            certificate = cryptoService.getTrustStore().getCertificate(senderAlias);
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
            LOG.error(e.getMessage(), e);
            return;
        }

        try {
            authenticate(submission);
            sendPayload(submission);
            Submission submissionResponse = getSubmissionResponse(submission, HAPPY_FLOW_MESSAGE_TEMPLATE.replace("$messId", messageId));
            messageSubmitter.submit(submissionResponse, getName());
        } catch (AuthenticationException e) {
            Submission submissionResponseError = getSubmissionResponse(submission, UMDS_REJECTED_TEMPLATE.replace("$messId", messageId));
            try {
                messageSubmitter.submit(submissionResponseError, getName());
            } catch (MessagingProcessingException e1) {
                LOG.error(e1.getMessage(), e1);
            }
        } catch (MessagingProcessingException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Submission getSubmissionResponse(Submission submission, String message) {
        accessPointHelper.switchAccessPoint(submission);
        endPointHelper.switchEndPoint(submission);
        submission.setMessageId(messageIdGenerator.generateMessageId());
        submission.getPayloads().clear();
        submission.addPayload(getPayload(message, MediaType.TEXT_XML));
        return submission;
    }

    public Submission.Payload getPayload(final String payloadContent, final String mediaType) {
        javax.mail.util.ByteArrayDataSource dataSource = new javax.mail.util.ByteArrayDataSource(payloadContent.getBytes(), mediaType);
        dataSource.setName("content.xml");
        DataHandler payLoadDataHandler = new DataHandler(dataSource);
        Submission.TypedProperty submissionTypedProperty = new Submission.TypedProperty(MIME_TYPE, mediaType);
        Collection<Submission.TypedProperty> listTypedProperty = new ArrayList<>();
        listTypedProperty.add(submissionTypedProperty);
        return new Submission.Payload(CID_MESSAGE, payLoadDataHandler, listTypedProperty, false, null, null);
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
        LOG.debug("Sending :");
        Map<String, Object> stringObjectMap = multipartRequest.toSingleValueMap();
        Set<Map.Entry<String, Object>> entries = stringObjectMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            LOG.debug("Key:[{}]", entry.getKey());
            LOG.debug("Value:[{}]", entry.getValue());
        }
        LOG.debug("To:[{}]", submissionRestUrl);
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
            LOG.debug("METRICS:[{}] messages send and return from c3 to c4 in [{}] seconds", metric.getCounter(), (double) (metric.lastChanged - metric.getStartTime()) / 1000);
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
