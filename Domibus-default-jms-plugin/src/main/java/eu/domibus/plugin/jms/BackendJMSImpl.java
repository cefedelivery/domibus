package eu.domibus.plugin.jms;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;
import eu.domibus.api.metrics.Metrics;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.receiver.MSHWebservice;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.JsonSubmission;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.Umds;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.taxud.CertificateLogging;
import eu.domibus.taxud.PayloadLogging;
import eu.domibus.wss4j.common.crypto.CryptoService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsOperations;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.jms.MapMessage;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.text.MessageFormat;
import java.util.*;

import static com.codahale.metrics.MetricRegistry.name;
import static eu.domibus.api.metrics.Metrics.METRIC_REGISTRY;
import static eu.domibus.ebms3.common.model.Property.MIME_TYPE;
import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_ID;
import static eu.domibus.plugin.jms.JMSMessageConstants.MESSAGE_TYPE_SUBMIT;

/**
 * @author Christian Koch, Stefan Mueller
 */

public class BackendJMSImpl extends AbstractBackendConnector<MapMessage, MapMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendJMSImpl.class);

    public final static String ORIGINAL_SENDER = "originalSender";

    public final static String FINAL_RECIPIENT = "finalRecipient";

    public final static String DELEGATOR = "delegator";

    public static final String AUTHENTICATION_ENDPOINT_PROPERTY_NAME = "domibus.c4.rest.authenticate.endpoint";

    public static final String PAYLOAD_ENDPOINT_PROPERTY_NAME = "domibus.c4.rest.payload.endpoint";

    public static final String CID_MESSAGE = "cid:message";

    public static final String DOMIBUS_TAXUD_CUST_DOMAIN = "domibus.taxud.cust.domain";

    public static final String DOMIBUS_DO_NOT_DELIVER = "domibus.do.not.deliver";

    public static final String DOMIBUS_DO_NOT_SEND_TO_C4 = "domibus.do.not.send.to.c4";


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
    private AccessPointHelper accessPointHelper;

    @Autowired
    private EndPointHelper endPointHelper;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private MessageIdGenerator messageIdGenerator;

    @Autowired
    private IdentifierHelper identifierHelper;

    @Autowired
    private DomainCoreConverter coreConverter;

    @Autowired
    private PayloadLogging payloadLogging;

    @Autowired
    private CertificateLogging certificateLogging;

    @Autowired
    private PModeProvider pModeProvider;

    private MessageRetrievalTransformer<MapMessage> messageRetrievalTransformer;

    private MessageSubmissionTransformer<MapMessage> messageSubmissionTransformer;

    private org.springframework.web.client.RestTemplate restTemplate;

    private static final Meter requestsPerSecond = Metrics.METRIC_REGISTRY.meter(name(MSHWebservice.class, "c3-c4-throughput"));

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
        final Timer.Context handleMessageContext = Metrics.METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "receiveMessage")).time();
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
        } finally {
            handleMessageContext.stop();
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

    private Submission.TypedProperty extractEndPoint(Submission submission, final String endPointType) {
        Submission.TypedProperty originalSender = null;
        Collection<Submission.TypedProperty> properties = submission.getMessageProperties();
        for (Submission.TypedProperty property : properties) {
            if (endPointType.equals(property.getKey())) {
                originalSender = property;
                break;
            }
        }
        return originalSender;
    }


    protected void authenticate(Submission submission) {
        String senderAlias;
        Timer.Context senderAliasContext = METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "authenticate.getSenderAlias")).time();
        senderAlias = getSenderAlias(submission);
        senderAliasContext.stop();
        Timer.Context buildUmds = METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "authenticate.buildUmds")).time();
        Umds umds = buidUmds(submission);
        LOG.info("Authentication submission for :\n   [{}]", umds);
        buildUmds.stop();
        Timer.Context encodeCertificate = METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "authenticate.encodeCertificate")).time();
        byte[] encodedCertificate = encodeCertificate(senderAlias);
        umds.setCertficiate(encodedCertificate);

        encodeCertificate.stop();
        String authenticationUrl = domibusProperties.getProperty(AUTHENTICATION_ENDPOINT_PROPERTY_NAME);

        Timer.Context authenticationPost = METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "authenticate.postMultipartRequest")).time();
        Boolean aBoolean = restTemplate.postForObject(authenticationUrl, umds, Boolean.class);
        authenticationPost.stop();
        if (!aBoolean) {
            LOG.error("UMDS rejected authentication for message with id[{}]", submission.getMessageId());
            throw new AuthenticationException("UMDS rejected authentication");
        }
    }

    private Umds buidUmds(final Submission submission) {
        final Submission.TypedProperty originalSender = extractEndPoint(submission, ORIGINAL_SENDER);
        final Submission.TypedProperty finalRecipient = extractEndPoint(submission, FINAL_RECIPIENT);
        final Submission.TypedProperty delegator = extractEndPoint(submission, DELEGATOR);
        Umds umds = identifierHelper.buildUmdsFromOriginalSender(originalSender.getValue());
        umds.setApplicationUrl(identifierHelper.getApplicationUrl(finalRecipient.getValue()));
        if (delegator != null) {
            identifierHelper.updateDelegatorInfo(umds, delegator.getValue());
        }

        String domain = "TAX";
        String custServiceAndActionMapping = domibusProperties.getProperty(DOMIBUS_TAXUD_CUST_DOMAIN, "");
        if (custServiceAndActionMapping.equals(submission.getService() + submission.getAction())) {
            domain = "CUST";
        }
        umds.setDomain(domain);
        return umds;
    }

    private byte[] encodeCertificate(String senderAlias) {
        try {
            Certificate certificate;
            certificate = cryptoService.getTrustStore().getCertificate(senderAlias);
            byte[] encodedCertificate = certificate.getEncoded();
            certificateLogging.log(encodedCertificate);
            return Base64.encodeBase64(encodedCertificate);
        } catch (CertificateEncodingException | KeyStoreException e) {
            LOG.error(e.getMessage(), e);
            throw new AuthenticationException("Error while extracting certificate", e);
        }
    }

    private String getSenderAlias(Submission submission) {
        Submission.Party party = accessPointHelper.extractSendingAccessPoint(submission);
        PartyId partyId = new PartyId();
        partyId.setValue(party.getPartyId());
        partyId.setType(party.getPartyIdType());
        try {
            return pModeProvider.findPartyName(Lists.newArrayList(partyId));
        } catch (EbMS3Exception e) {
            LOG.error("No alias found for sender [{}]", partyId);
            throw new AuthenticationException("No alias found for sender");
        }
    }

    @Override
    public void deliverMessage(final String messageId) {
        final Timer.Context handleMessageContext = Metrics.METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "deliverMessage")).time();
        if (Boolean.valueOf(domibusProperties.getProperty(DOMIBUS_DO_NOT_DELIVER, "false"))) {
            LOG.warn("Skipping delivery.");
            try {
                this.messageRetriever.downloadMessage(messageId);
            } catch (MessageNotFoundException e) {
                LOG.error(e.getMessage(), e);
            }
            return;
        }
        Submission submission;
        Timer.Context downloadMessageContext = null;
        try {
            downloadMessageContext = METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "downloadMessage")).time();
            submission = this.messageRetriever.downloadMessage(messageId);
        } catch (MessageNotFoundException e) {
            LOG.error(e.getMessage(), e);
            return;
        } finally {
            downloadMessageContext.stop();
        }

        try {
            if (!Boolean.valueOf(domibusProperties.getProperty(DOMIBUS_DO_NOT_SEND_TO_C4, "true"))) {
                Timer.Context authenticateContext = METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "authenticate")).time();
                authenticate(submission);
                authenticateContext.stop();
                Timer.Context uploadPayload = METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "uploadPayload")).time();
                sendPayload(submission);
                uploadPayload.stop();
            }

        } catch (AuthenticationException e) {
            Submission submissionResponseError = getSubmissionResponse(submission, UMDS_REJECTED_TEMPLATE.replace("$messId", messageId));
            try {
                messageSubmitter.submit(submissionResponseError, getName());
            } catch (MessagingProcessingException e1) {
                LOG.error(e1.getMessage(), e1);
            }
        } finally {
            handleMessageContext.stop();
        }
    }

    private Submission getSubmissionResponse(Submission submission, String message) {
        accessPointHelper.switchAccessPoint(submission);
        endPointHelper.switchEndPoint(submission);
        submission.setRefToMessageId(org.apache.commons.lang.StringUtils.trim(submission.getMessageId()));
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
        Timer.Context convertJsonContext = METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "sendPayload.convertJson")).time();
        JsonSubmission jsonSubmission = coreConverter.convert(submission, JsonSubmission.class);
        Set<Submission.Party> toParties = submission.getToParties();
        jsonSubmission.setFromRole(submission.getFromRole());
        jsonSubmission.setToRole(submission.getToRole());

        for (Submission.Party toParty : toParties) {
            jsonSubmission.addToParty(toParty.getPartyId(), toParty.getPartyIdType());
        }

        Set<Submission.Party> fromParties = submission.getFromParties();
        for (Submission.Party fromParty : fromParties) {
            jsonSubmission.addFromParty(fromParty.getPartyId(), fromParty.getPartyIdType());
        }
        Collection<Submission.TypedProperty> messageProperties = submission.getMessageProperties();
        for (Submission.TypedProperty messageProperty : messageProperties) {
            jsonSubmission.getMessageProperties().add(new JsonSubmission.TypedProperty(messageProperty.getKey(), messageProperty.getValue(), messageProperty.getType()));
        }

        convertJsonContext.stop();
        LOG.info("Message submission:\n  [{}]", jsonSubmission);
        Timer.Context buildRequestFromPayloadContext = METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "sendPayload.buildRequestFromPayload")).time();
        byte[] bytes = buildMultiPartRequestFromPayload(submission.getPayloads());
        jsonSubmission.setPayload(bytes);

        buildRequestFromPayloadContext.stop();
        String payloadEndPointUrl = domibusProperties.getProperty(PAYLOAD_ENDPOINT_PROPERTY_NAME);

        Timer.Context postPayloadContext = METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "sendPayload.postPayload")).time();
        restTemplate.postForLocation(payloadEndPointUrl, jsonSubmission);
        requestsPerSecond.mark();
        postPayloadContext.stop();
    }

    private byte[] buildMultiPartRequestFromPayload(Set<Submission.Payload> payloads) {
        for (Submission.Payload payload : payloads) {
            try (InputStream inputStream = payload.getPayloadDatahandler().getInputStream();) {

                int available = inputStream.available();
                if (available == 0) {
                    LOG.warn("Payload skipped because it is empty");
                    return new byte[]{0};
                }
                byte[] payloadContent = new byte[available];
                inputStream.read(payloadContent);
                payloadLogging.log(payloadContent);
                return Base64.encodeBase64(payloadContent);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return new byte[]{0};
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
        final Timer.Context handleMessageContext = Metrics.METRIC_REGISTRY.timer(name(BackendJMSImpl.class, "messageSendSuccess")).time();
        replyJmsTemplate.send(new SignalMessageCreator(messageId, NotificationType.MESSAGE_SEND_SUCCESS));
        handleMessageContext.stop();
    }

}
