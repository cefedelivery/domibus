package eu.domibus.plugin.handler;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.impl.SubmissionAS4Transformer;
import eu.domibus.plugin.webService.generated.SendMessageFault;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.input.XmlStreamReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Queue;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * @author Federico Martini
 * @since 3.2
 */
@RunWith(JMockit.class)
public class DatabaseMessageHandlerTest {

    private static final Log LOG = LogFactory.getLog(DatabaseMessageHandlerTest.class);
    private static final String BACKEND = "backend";
    private static final String DEF_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String STRING_TYPE = "string";
    private static final String MESS_ID = "cf012f4c-5a31-4759-ad9c-1d12331420656@domibus.eu";
    private static final String DOMIBUS_GREEN = "domibus-green";
    private static final String DOMIBUS_RED = "domibus-red";
    private static final String GREEN = "green_gw";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";

    @Tested
    private DatabaseMessageHandler dmh;

    @Injectable
    JMSManager jmsManager;

    @Injectable
    private Queue sendMessageQueue;

    @Injectable
    private CompressionService compressionService;

    @Injectable
    private SubmissionAS4Transformer transformer;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private SignalMessageDao signalMessageDao;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    @Injectable
    private SignalMessageLogDao signalMessageLogDao;

    @Injectable
    private ErrorLogDao errorLogDao;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private MessageIdGenerator messageIdGenerator;

    @Injectable
    private PayloadProfileValidator payloadProfileValidator;

    @Injectable
    private PropertyProfileValidator propertyProfileValidator;

    @Injectable
    AuthUtils authUtils;


    protected Property createProperty(String name, String value, String type) {
        Property aProperty = new Property();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(type);
        return aProperty;
    }

    protected UserMessage createUserMessage() {
        UserMessage userMessage = new UserMessage();
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setAction("TC2Leg1");
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setValue("");
        collaborationInfo.setAgreementRef(agreementRef);
        Service service = new Service();
        service.setValue("bdx:noprocess");
        service.setType("tc1");
        collaborationInfo.setService(service);
        userMessage.setCollaborationInfo(collaborationInfo);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getProperty().add(createProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", STRING_TYPE));
        messageProperties.getProperty().add(createProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4", STRING_TYPE));
        userMessage.setMessageProperties(messageProperties);
        PartyInfo partyInfo = new PartyInfo();
        From from = new From();
        from.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        PartyId sender = new PartyId();
        sender.setValue(DOMIBUS_GREEN);
        sender.setType(DEF_PARTY_TYPE);
        partyInfo.setFrom(from);
        To to = new To();
        to.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        PartyId receiver = new PartyId();
        receiver.setValue(DOMIBUS_RED);
        receiver.setType(DEF_PARTY_TYPE);
        partyInfo.setTo(to);

        userMessage.setPartyInfo(partyInfo);

        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = new PartInfo();
        partInfo.setHref("cid:message");
/*        Description description = new Description();
        description.setValue("e-sens-sbdh-order");
        description.setLang("en-US");
        partInfo.setDescription(description);
*/

        PartProperties partProperties = new PartProperties();
        partProperties.getProperties().add(createProperty("text/xml", "MimeType", STRING_TYPE));
        partInfo.setPartProperties(partProperties);

        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        return userMessage;
    }

    private Configuration readConf() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("SamplePModes/domibus-configuration-valid_green.xml");
        Unmarshaller unMar = JAXBContext.newInstance("eu.domibus.common.model.configuration").createUnmarshaller();
        return (Configuration) unMar.unmarshal(new XmlStreamReader(is));
    }

    @Test
    public void testSubmitMessageGreen2RedOk(@Injectable final Submission messageData, @Injectable final Submission.Payload payload1) throws Exception {
        new Expectations() {{

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;

            String pModeKey = "green_gw:red_gw:testService1:TC2Leg1::pushTestcase1tc2Action";

            pModeProvider.findPModeKeyForUserMessage(userMessage);
            result = pModeKey;

            Party sender = new Party();
            sender.setName(GREEN);
            pModeProvider.getSenderParty(pModeKey);
            result = sender;

            Party receiver = new Party();
            receiver.setName(RED);
            pModeProvider.getReceiverParty(pModeKey);
            result = receiver;

            Configuration conf = new Configuration();
            Party confParty = new Party();
            confParty.setName(GREEN);
            conf.setParty(confParty);
            pModeProvider.getConfigurationDAO().read();
            result = conf;
        }};

        String messageId = dmh.submit(messageData, BACKEND);
        Assert.assertEquals(messageId, MESS_ID);

        new Verifications() {{
            messageIdGenerator.generateMessageId();
        }};

    }

    @Test
    public void testSubmitMessageGreen2RedNOk(@Injectable final Submission messageData, @Injectable final Submission.Payload payload1) throws Exception {
        new Expectations() {{

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;

            String pModeKey = "green_gw:red_gw:testService1:TC2Leg1::pushTestcase1tc2Action";

            pModeProvider.findPModeKeyForUserMessage(userMessage);
            result = pModeKey;

            Party sender = new Party();
            sender.setName(GREEN);
            pModeProvider.getSenderParty(pModeKey);
            result = sender;

            Party receiver = new Party();
            receiver.setName(RED);
            pModeProvider.getReceiverParty(pModeKey);
            result = receiver;

            // Here the configuration of the access point is supposed to be BLUE!
            Configuration conf = new Configuration();
            Party confParty = new Party();
            confParty.setName(BLUE);
            conf.setParty(confParty);
            Configuration config = pModeProvider.getConfigurationDAO().read();
            config.getParty();
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010,
                    "The initiator party's name [green_gw] does not correspond to the access point's name [blue_gw]",
                    null,
                    null);

        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException ex) {
            LOG.debug("MessagingProcessingException catched " + ex.getMessage());
        }
    }

    @Test
    public void testDownloadMessageOK() throws SendMessageFault, InterruptedException, SQLException {
        //TODO

    }


}
