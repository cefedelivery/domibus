package eu.domibus.controller;


import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import eu.domibus.example.ws.WebserviceExample;
import eu.domibus.plugin.JsonSubmission;
import eu.domibus.plugin.webService.generated.LargePayloadType;
import eu.domibus.plugin.webService.generated.SendMessageFault;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import eu.domibus.taxud.IdentifierUtil;
import eu.domibus.taxud.PayloadLogging;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.activation.DataHandler;
import javax.annotation.PostConstruct;
import javax.ws.rs.core.MediaType;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.UUID;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RestController
public class IstController {

    private final static Logger LOG = LoggerFactory.getLogger(IstController.class);

    private final static String ORIGINAL_SENDER = "originalSender";

    private final static String FINAL_RECIPIENT = "finalRecipient";

    private static final String CID_MESSAGE = "cid:message";

    private static final String MIME_TYPE = "MimeType";

    private static final String TEXT_XML = "text/xml";

    private PayloadLogging payloadLogging;

    private AccessPointHelper accessPointHelper;

    private EndPointHelper endPointHelper;


    @Value("${domibus.pull.user.identifier}")
    private String pullUserIdentifier;

/*    @Value("${domibus.pull.action}")
    private String pullAction;

    @Value("${domibus.pull.service}")
    private String pullService;*/

    @Value("${domibus.pull.service.type}")
    private String pullServiceType;

    @Value("${domibus.do.not.push.back.to.c3}")
    private String doNotPushBackProperty;

    private boolean doNotPushBack;

    private WebserviceExample webserviceExample;

    private Observable<Submission> quoteObservable = null;

    private final static String HAPPY_FLOW_MESSAGE_TEMPLATE = "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<response_to_message_id>\n" +
            " $messId\n" +
            "</response_to_message_id>\n" +
            "<dataset>\n" +
            "<record><id>1</id><first_name>Belita</first_name><last_name>MacMeanma</last_name><email>bmacmeanma0@alexa.com</email><gender>Female</gender><ip_address>211.210.105.141</ip_address></record><record><id>2</id><first_name>Delainey</first_name><last_name>Sarll</last_name><email>dsarll1@xinhuanet.com</email><gender>Male</gender><ip_address>172.215.113.41</ip_address></record><record><id>3</id><first_name>Rafaela</first_name><last_name>Jandel</last_name><email>rjandel2@usda.gov</email><gender>Female</gender><ip_address>176.76.130.69</ip_address></record><record><id>4</id><first_name>Fredrika</first_name><last_name>Dunbabin</last_name><email>fdunbabin3@google.com.br</email><gender>Female</gender><ip_address>28.5.174.234</ip_address></record><record><id>5</id><first_name>Othilie</first_name><last_name>Braniff</last_name><email>obraniff4@redcross.org</email><gender>Female</gender><ip_address>135.122.142.137</ip_address></record><record><id>6</id><first_name>Filmer</first_name><last_name>Wands</last_name><email>fwands5@newsvine.com</email><gender>Male</gender><ip_address>245.77.82.100</ip_address></record><record><id>7</id><first_name>Bernie</first_name><last_name>Le feaver</last_name><email>blefeaver6@usda.gov</email><gender>Male</gender><ip_address>142.226.208.76</ip_address></record><record><id>8</id><first_name>Dacy</first_name><last_name>Di Antonio</last_name><email>ddiantonio7@bbb.org</email><gender>Female</gender><ip_address>235.214.118.96</ip_address></record><record><id>9</id><first_name>Hobey</first_name><last_name>Di Pietro</last_name><email>hdipietro8@nps.gov</email><gender>Male</gender><ip_address>166.30.27.83</ip_address></record><record><id>10</id><first_name>Catha</first_name><last_name>Denkel</last_name><email>cdenkel9@princeton.edu</email><gender>Female</gender><ip_address>102.60.69.38</ip_address></record><record><id>11</id><first_name>Jeralee</first_name><last_name>Gorling</last_name><email>jgorlinga@google.ca</email><gender>Female</gender><ip_address>217.169.183.180</ip_address></record><record><id>12</id><first_name>Henrietta</first_name><last_name>Aloshechkin</last_name><email>haloshechkinb@umich.edu</email><gender>Female</gender><ip_address>128.2.221.166</ip_address></record><record><id>13</id><first_name>Georges</first_name><last_name>Veregan</last_name><email>gvereganc@seattletimes.com</email><gender>Male</gender><ip_address>117.64.187.183</ip_address></record><record><id>14</id><first_name>Dara</first_name><last_name>Shottin</last_name><email>dshottind@weather.com</email><gender>Female</gender><ip_address>167.185.3.185</ip_address></record><record><id>15</id><first_name>Jerry</first_name><last_name>Attrill</last_name><email>jattrille@nps.gov</email><gender>Male</gender><ip_address>144.46.79.18</ip_address></record><record><id>16</id><first_name>Worth</first_name><last_name>Louche</last_name><email>wlouchef@vkontakte.ru</email><gender>Male</gender><ip_address>17.117.2.116</ip_address></record><record><id>17</id><first_name>Gabie</first_name><last_name>Fontel</last_name><email>gfontelg@nbcnews.com</email><gender>Female</gender><ip_address>94.216.217.36</ip_address></record><record><id>18</id><first_name>Stanton</first_name><last_name>Millott</last_name><email>smillotth@google.nl</email><gender>Male</gender><ip_address>6.194.119.179</ip_address></record><record><id>19</id><first_name>Hedi</first_name><last_name>Pele</last_name><email>hpelei@jiathis.com</email><gender>Female</gender><ip_address>198.140.7.33</ip_address></record><record><id>20</id><first_name>Nils</first_name><last_name>Klesl</last_name><email>nkleslj@woothemes.com</email><gender>Male</gender><ip_address>106.74.129.90</ip_address></record><record><id>21</id><first_name>Bucky</first_name><last_name>Hobbema</last_name><email>bhobbemak@livejournal.com</email><gender>Male</gender><ip_address>173.139.210.39</ip_address></record><record><id>22</id><first_name>Araldo</first_name><last_name>Claye</last_name><email>aclayel@elpais.com</email><gender>Male</gender><ip_address>116.15.8.224</ip_address></record><record><id>23</id><first_name>Jules</first_name><last_name>Heninghem</last_name><email>jheninghemm@biblegateway.com</email><gender>Male</gender><ip_address>196.24.132.34</ip_address></record><record><id>24</id><first_name>Trista</first_name><last_name>Kiloh</last_name><email>tkilohn@npr.org</email><gender>Female</gender><ip_address>108.148.209.172</ip_address></record><record><id>25</id><first_name>Clevie</first_name><last_name>Drinkall</last_name><email>cdrinkallo@blogtalkradio.com</email><gender>Male</gender><ip_address>63.122.167.93</ip_address></record><record><id>26</id><first_name>Monte</first_name><last_name>Deary</last_name><email>mdearyp@fc2.com</email><gender>Male</gender><ip_address>170.13.123.223</ip_address></record><record><id>27</id><first_name>Teresina</first_name><last_name>Keuning</last_name><email>tkeuningq@ask.com</email><gender>Female</gender><ip_address>29.193.166.64</ip_address></record><record><id>28</id><first_name>Noam</first_name><last_name>Muckley</last_name><email>nmuckleyr@cbc.ca</email><gender>Male</gender><ip_address>246.237.66.187</ip_address></record><record><id>29</id><first_name>Cordelia</first_name><last_name>Bussens</last_name><email>cbussenss@artisteer.com</email><gender>Female</gender><ip_address>102.234.75.160</ip_address></record><record><id>30</id><first_name>Henrik</first_name><last_name>Paffley</last_name><email>hpaffleyt@upenn.edu</email><gender>Male</gender><ip_address>246.79.215.136</ip_address></record><record><id>31</id><first_name>Branden</first_name><last_name>Stannett</last_name><email>bstannettu@yahoo.com</email><gender>Male</gender><ip_address>161.122.87.149</ip_address></record><record><id>32</id><first_name>Madelle</first_name><last_name>Drayton</last_name><email>mdraytonv@tmall.com</email><gender>Female</gender><ip_address>69.170.17.15</ip_address></record><record><id>33</id><first_name>Flemming</first_name><last_name>Hastie</last_name><email>fhastiew@statcounter.com</email><gender>Male</gender><ip_address>194.30.236.45</ip_address></record><record><id>34</id><first_name>Torrance</first_name><last_name>Mielnik</last_name><email>tmielnikx@home.pl</email><gender>Male</gender><ip_address>130.163.101.62</ip_address></record><record><id>35</id><first_name>Cinnamon</first_name><last_name>Trevor</last_name><email>ctrevory@boston.com</email><gender>Female</gender><ip_address>132.206.141.48</ip_address></record><record><id>36</id><first_name>Deanne</first_name><last_name>Gullen</last_name><email>dgullenz@rambler.ru</email><gender>Female</gender><ip_address>134.61.119.145</ip_address></record><record><id>37</id><first_name>Wyatan</first_name><last_name>Rudgard</last_name><email>wrudgard10@addthis.com</email><gender>Male</gender><ip_address>119.131.19.119</ip_address></record><record><id>38</id><first_name>Thomasa</first_name><last_name>Keme</last_name><email>tkeme11@storify.com</email><gender>Female</gender><ip_address>29.51.65.34</ip_address></record><record><id>39</id><first_name>Mead</first_name><last_name>Cobain</last_name><email>mcobain12@youtu.be</email><gender>Female</gender><ip_address>177.138.6.69</ip_address></record><record><id>40</id><first_name>Baillie</first_name><last_name>Sommerlie</last_name><email>bsommerlie13@home.pl</email><gender>Male</gender><ip_address>46.91.193.197</ip_address></record><record><id>41</id><first_name>Cindi</first_name><last_name>Waldocke</last_name><email>cwaldocke14@nature.com</email><gender>Female</gender><ip_address>211.123.179.43</ip_address></record><record><id>42</id><first_name>Sophie</first_name><last_name>Weddell</last_name><email>sweddell15@tiny.cc</email><gender>Female</gender><ip_address>92.79.6.93</ip_address></record><record><id>43</id><first_name>Faydra</first_name><last_name>Spata</last_name><email>fspata16@bloomberg.com</email><gender>Female</gender><ip_address>3.85.1.239</ip_address></record><record><id>44</id><first_name>Monte</first_name><last_name>Philipeau</last_name><email>mphilipeau17@examiner.com</email><gender>Male</gender><ip_address>49.233.30.244</ip_address></record><record><id>45</id><first_name>Garrott</first_name><last_name>Creer</last_name><email>gcreer18@webnode.com</email><gender>Male</gender><ip_address>253.166.143.212</ip_address></record><record><id>46</id><first_name>Harp</first_name><last_name>Wherrett</last_name><email>hwherrett19@squarespace.com</email><gender>Male</gender><ip_address>197.232.85.3</ip_address></record><record><id>47</id><first_name>Miller</first_name><last_name>Wilsee</last_name><email>mwilsee1a@wix.com</email><gender>Male</gender><ip_address>242.106.77.87</ip_address></record><record><id>48</id><first_name>Prentiss</first_name><last_name>Tucknott</last_name><email>ptucknott1b@wix.com</email><gender>Male</gender><ip_address>107.41.137.99</ip_address></record><record><id>49</id><first_name>Muffin</first_name><last_name>Mulkerrins</last_name><email>mmulkerrins1c@cisco.com</email><gender>Female</gender><ip_address>219.94.140.169</ip_address></record><record><id>50</id><first_name>Tamera</first_name><last_name>Skade</last_name><email>tskade1d@flavors.me</email><gender>Female</gender><ip_address>140.28.170.139</ip_address></record>\n" +
            "</dataset>";

    @Autowired
    public IstController(final PayloadLogging payloadLogging,
                         final WebserviceExample webserviceExample,
                         final AccessPointHelper accessPointHelper,
                         final EndPointHelper endPointHelper) {
        this.payloadLogging = payloadLogging;
        this.webserviceExample = webserviceExample;
        this.accessPointHelper = accessPointHelper;
        this.endPointHelper = endPointHelper;
    }

    @PostConstruct
    protected void init(){
        doNotPushBack=Boolean.valueOf(doNotPushBackProperty);
        LOG.warn("Do not push to c3:[{}]",doNotPushBack);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/message", produces = "application/json")
    public void onMessage(@RequestBody JsonSubmission jsonSubmission) {
        LOG.info("Message received with id:\n  [{}]", jsonSubmission.getMessageId());
        payloadLogging.decodeAndlog(jsonSubmission.getPayload());
        if(!doNotPushBack) {
            Observable<Submission> quoteObservable = Observable.<Submission>create(subscriber -> {
                Submission submission = prepareSubmission(jsonSubmission);
                subscriber.onNext(submission);
                subscriber.onComplete();
            }).subscribeOn(Schedulers.io());
            quoteObservable.subscribe(this::sendMessage);
        }
    }

    private void sendMessage(Submission submission){
        try {
            webserviceExample.getPort().submitMessage(submission.getSubmitRequest(), submission.getMessaging());
        } catch (SendMessageFault|MalformedURLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Submission prepareSubmission(JsonSubmission submission){
        String response = HAPPY_FLOW_MESSAGE_TEMPLATE.replace("$messId", submission.getMessageId());
        accessPointHelper.switchAccessPoint(submission);
        endPointHelper.switchEndPoint(submission);

        final JsonSubmission.TypedProperty originalSender = extractEndPoint(submission, ORIGINAL_SENDER);
        final JsonSubmission.TypedProperty finalRecipient = extractEndPoint(submission, FINAL_RECIPIENT);

        //create payload.
        LargePayloadType payloadType = new LargePayloadType();
        payloadType.setPayloadId(CID_MESSAGE);
        payloadType.setContentType(MediaType.TEXT_XML);
        payloadType.setValue(getPayload(response, MediaType.TEXT_XML));

        //setup submit request.
        SubmitRequest submitRequest = new SubmitRequest();
        submitRequest.getPayload().add(payloadType);

        //setup messaging.
        Messaging messaging = new Messaging();
        UserMessage userMessage = new UserMessage();
        MessageInfo responseMessageInfo = new MessageInfo();
        //responseMessageInfo.setMessageId(UUID.randomUUID() + "@domibus");
        responseMessageInfo.setRefToMessageId(submission.getMessageId());

        userMessage.setMessageInfo(responseMessageInfo);
        PartyInfo partyInfo = new PartyInfo();
        userMessage.setPartyInfo(partyInfo);


        JsonSubmission.Party submissionFrom = submission.getFromParties().iterator().next();
        JsonSubmission.Party submissionTo = submission.getToParties().iterator().next();
        String submissionService = submission.getService();
        String submissionServiceType = submission.getServiceType();
        String submissionAction = submission.getAction();

        From responseFrom = new From();
        responseFrom.setRole(submission.getToRole());
        partyInfo.setFrom(responseFrom);
        PartyId responseFromPartyId = new PartyId();
        responseFromPartyId.setValue(submissionFrom.getPartyId());
        responseFromPartyId.setType(submissionFrom.getPartyIdType());
        responseFrom.setPartyId(responseFromPartyId);

        To responseTo = new To();
        responseTo.setRole(submission.getFromRole());
        partyInfo.setTo(responseTo);
        PartyId responseToPartyId = new PartyId();
        String to_partyId = submissionTo.getPartyId();
        responseToPartyId.setValue(to_partyId);
        responseToPartyId.setType(submissionTo.getPartyIdType());
        responseTo.setPartyId(responseToPartyId);


        String[] splitIdentifier = IdentifierUtil.splitIdentifier(finalRecipient.getValue());
        if (splitIdentifier[1].equalsIgnoreCase(pullUserIdentifier)) {
            submissionServiceType = to_partyId+pullServiceType;
        }
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        Service responseService = new Service();
        responseService.setType(submissionServiceType);
        responseService.setValue(submissionService);
        collaborationInfo.setService(responseService);
        collaborationInfo.setAction(submissionAction);
        userMessage.setCollaborationInfo(collaborationInfo);

        MessageProperties responseMessageProperties = new MessageProperties();
        userMessage.setMessageProperties(responseMessageProperties);

        Property responseOriginalSender = new Property();
        responseMessageProperties.getProperty().add(responseOriginalSender);
        responseOriginalSender.setName(ORIGINAL_SENDER);
        responseOriginalSender.setValue(originalSender.getValue());
        responseOriginalSender.setType(originalSender.getType());

        Property responseFinalRecipient = new Property();
        responseMessageProperties.getProperty().add(responseFinalRecipient);
        responseFinalRecipient.setName(FINAL_RECIPIENT);
        responseFinalRecipient.setValue(finalRecipient.getValue());
        responseFinalRecipient.setType(finalRecipient.getType());

        PayloadInfo responsePayloadInfo = new PayloadInfo();
        userMessage.setPayloadInfo(responsePayloadInfo);

        PartInfo responsePartInfo = new PartInfo();
        responsePayloadInfo.getPartInfo().add(responsePartInfo);
        responsePartInfo.setHref(CID_MESSAGE);
        PartProperties responsePartProperty = new PartProperties();
        Property responsePartInfoProperty = new Property();
        responsePartProperty.getProperty().add(responsePartInfoProperty);
        responsePartInfo.setPartProperties(responsePartProperty);
        responsePartInfoProperty.setName(MIME_TYPE);
        responsePartInfoProperty.setValue(TEXT_XML);
        messaging.setUserMessage(userMessage);
        return new Submission(submitRequest,messaging);
    }

    static class Submission{
        private SubmitRequest submitRequest;
        private Messaging messaging;

        public Submission(SubmitRequest submitRequest, Messaging messaging) {
            this.submitRequest = submitRequest;
            this.messaging = messaging;
        }

        public SubmitRequest getSubmitRequest() {
            return submitRequest;
        }

        public Messaging getMessaging() {
            return messaging;
        }
    }

    private JsonSubmission.TypedProperty extractEndPoint(JsonSubmission submission, final String endPointType) {
        JsonSubmission.TypedProperty originalSender = null;
        Collection<JsonSubmission.TypedProperty> properties = submission.getMessageProperties();
        for (JsonSubmission.TypedProperty property : properties) {
            if (endPointType.equals(property.getKey())) {
                originalSender = property;
                break;
            }
        }
        return originalSender;
    }

    private DataHandler getPayload(final String payloadContent, final String mediaType) {
        javax.mail.util.ByteArrayDataSource dataSource = null;
        dataSource = new javax.mail.util.ByteArrayDataSource(org.apache.commons.codec.binary.Base64.encodeBase64(payloadContent.getBytes()), mediaType);
        dataSource.setName("content.xml");
        return new DataHandler(dataSource);
    }


    //for testing purpose.
    @RequestMapping(value = "/message", method = RequestMethod.GET)
    public String onMessage() {
        return "Taxud ist is up";
    }


}
