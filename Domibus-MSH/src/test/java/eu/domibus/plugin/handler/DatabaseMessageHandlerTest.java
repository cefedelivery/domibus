package eu.domibus.plugin.handler;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.security.util.AuthUtils;
import eu.domibus.messaging.DuplicateMessageException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.impl.SubmissionAS4Transformer;
import eu.domibus.plugin.webService.generated.SendMessageFault;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.jms.Queue;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;

/**
 * @author Federico Martini
 * @since 3.2
 *
 * in the Verifications() the execution "times" is by default 1.
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
    private MessagingService messagingService;

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
        from.getPartyId().add(sender);
        partyInfo.setFrom(from);

        To to = new To();
        to.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");

        PartyId receiver = new PartyId();
        receiver.setValue(DOMIBUS_RED);
        receiver.setType(DEF_PARTY_TYPE);
        to.getPartyId().add(receiver);
        partyInfo.setTo(to);

        userMessage.setPartyInfo(partyInfo);

        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = new PartInfo();
        partInfo.setHref("cid:message");

        PartProperties partProperties = new PartProperties();
        partProperties.getProperties().add(createProperty("text/xml", "MimeType", STRING_TYPE));
        partInfo.setPartProperties(partProperties);

        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        return userMessage;
    }

/*    private Configuration readConf() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("SamplePModes/domibus-configuration-valid_green.xml");
        Unmarshaller unMar = JAXBContext.newInstance("eu.domibus.common.model.configuration").createUnmarshaller();
        return (Configuration) unMar.unmarshal(new XmlStreamReader(is));
    }*/

    @Test
    public void testSubmitMessageGreen2RedOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

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

            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            compressionService.handleCompression(userMessage, legConfiguration);
            result = true;
        }};

        String messageId = dmh.submit(messageData, BACKEND);
        assertEquals(messageId, MESS_ID);

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findPModeKeyForUserMessage(withAny(new UserMessage()));
            pModeProvider.getLegConfiguration(anyString);
            compressionService.handleCompression(withAny(new UserMessage()), withAny(new LegConfiguration()));
            messagingService.storeMessage(withAny(new Messaging()));
            userMessageLogDao.create(withAny(new UserMessageLog()));
        }};

    }

    @Test
    public void testSubmitMessageWithRefIdGreen2RedOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = createUserMessage();
            userMessage.getMessageInfo().setRefToMessageId("abc012f4c-5a31-4759-ad9c-1d12331420656@domibus.eu");
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

            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            compressionService.handleCompression(userMessage, legConfiguration);
            result = true;
        }};

        String messageId = dmh.submit(messageData, BACKEND);
        assertEquals(messageId, MESS_ID);

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findPModeKeyForUserMessage(withAny(new UserMessage()));
            pModeProvider.getLegConfiguration(anyString);
            compressionService.handleCompression(withAny(new UserMessage()), withAny(new LegConfiguration()));
            messagingService.storeMessage(withAny(new Messaging()));
            userMessageLogDao.create(withAny(new UserMessageLog()));
        }};

    }

    @Test
    public void testSubmitMessageWithIdNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = new UserMessage();
            userMessage.getMessageInfo().setMessageId("abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656@domibus.eu");
            transformer.transformFromSubmission(messageData);
            result = userMessage;

        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(mpEx.getEbms3ErrorCode(), ErrorCode.EBMS_0008);
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            messageIdGenerator.generateMessageId();
            times = 0;
            userMessageLogDao.getMessageStatus(MESS_ID);
            times = 0;
            pModeProvider.findPModeKeyForUserMessage(withAny(new UserMessage()));
            times = 0;
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagingService.storeMessage(withAny(new Messaging()));
            times = 0;
            userMessageLogDao.create(withAny(new UserMessageLog()));
            times = 0;
        }};

    }


    @Test
    public void testSubmitMessageWithRefIdNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = new UserMessage();
            userMessage.getMessageInfo().setRefToMessageId("abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656@domibus.eu");
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(mpEx.getEbms3ErrorCode(), ErrorCode.EBMS_0008);
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            times = 0;
            pModeProvider.findPModeKeyForUserMessage(withAny(new UserMessage()));
            times = 0;
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagingService.storeMessage(withAny(new Messaging()));
            times = 0;
            userMessageLogDao.create(withAny(new UserMessageLog()));
            times = 0;
        }};

    }

    @Test
    public void testSubmitMessageGreen2RedNOk(@Injectable final Submission messageData) throws Exception {
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
            pModeProvider.getConfigurationDAO().read();
            result = conf;

        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(mpEx.getEbms3ErrorCode(), ErrorCode.EBMS_0010);
            assert (mpEx.getMessage().contains("does not correspond to the access point's name"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findPModeKeyForUserMessage(withAny(new UserMessage()));
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagingService.storeMessage(withAny(new Messaging()));
            times = 0;
            userMessageLogDao.create(withAny(new UserMessageLog()));
            times = 0;
        }};

    }

    @Test
    /**
     * Tests a submit message where from and to parties are the same.
     */
    public void testSubmitMessageBlue2BlueNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;

            String pModeKey = "blue_gw:red_gw:testService1:TC2Leg1::pushTestcase1tc2Action";

            pModeProvider.findPModeKeyForUserMessage(userMessage);
            result = pModeKey;

            Party sender = new Party();
            sender.setName(BLUE);
            pModeProvider.getSenderParty(pModeKey);
            result = sender;

            Party receiver = new Party();
            receiver.setName(BLUE);
            pModeProvider.getReceiverParty(pModeKey);
            result = receiver;

        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(mpEx.getEbms3ErrorCode(), ErrorCode.EBMS_0010);
            assert (mpEx.getMessage().contains("The initiator party's name is the same as the responder party's one"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findPModeKeyForUserMessage(withAny(new UserMessage()));
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagingService.storeMessage(withAny(new Messaging()));
            times = 0;
            userMessageLogDao.create(withAny(new UserMessageLog()));
            times = 0;
        }};
    }

    @Test
    /**
     * Tests when a message is sent to BLUE and the configuration of the SENDING access point is BLUE.
     */
    public void testSubmitMessageGreen2BlueNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;

            String pModeKey = "green_gw:blue_gw:testService1:TC2Leg1::pushTestcase1tc2Action";

            pModeProvider.findPModeKeyForUserMessage(userMessage);
            result = pModeKey;

            Party sender = new Party();
            sender.setName(GREEN);
            pModeProvider.getSenderParty(pModeKey);
            result = sender;

            Party receiver = new Party();
            receiver.setName(BLUE);
            pModeProvider.getReceiverParty(pModeKey);
            result = receiver;

            // Here the configuration of the access point is supposed to be BLUE!
            Configuration conf = new Configuration();
            Party confParty = new Party();
            confParty.setName(BLUE);
            conf.setParty(confParty);
            pModeProvider.getConfigurationDAO().read();
            result = conf;

        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(mpEx.getEbms3ErrorCode(), ErrorCode.EBMS_0010);
            assert (mpEx.getMessage().contains("It is forbidden to submit a message to the sending access point"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findPModeKeyForUserMessage(withAny(new UserMessage()));
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagingService.storeMessage(withAny(new Messaging()));
            times = 0;
            userMessageLogDao.create(withAny(new UserMessageLog()));
            times = 0;
        }};
    }

    @Test
    public void testSubmitMessageCompressionNOk(@Injectable final Submission messageData) throws Exception {
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

            LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
            compressionService.handleCompression(userMessage, legConfiguration);
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, "No mime type found for payload with cid:message", MESS_ID, null);
        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(mpEx.getEbms3ErrorCode(), ErrorCode.EBMS_0303);
            assert (mpEx.getMessage().contains("No mime type found for payload with cid:"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findPModeKeyForUserMessage(withAny(new UserMessage()));
            pModeProvider.getLegConfiguration(anyString);
            compressionService.handleCompression(withAny(new UserMessage()), withAny(new LegConfiguration()));
            errorLogDao.create(withAny(new ErrorLogEntry()));
            messagingService.storeMessage(withAny(new Messaging()));
            times = 0;
            userMessageLogDao.create(withAny(new UserMessageLog()));
            times = 0;
        }};

    }

    @Test
    public void testSubmitMessagePModeNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;

            pModeProvider.findPModeKeyForUserMessage(userMessage);
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "PMode could not be found. Are PModes configured in the database?", MESS_ID, null);

        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(mpEx.getEbms3ErrorCode(), ErrorCode.EBMS_0010);
            assert (mpEx.getMessage().contains("PMode could not be found. Are PModes configured in the database?"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findPModeKeyForUserMessage(withAny(new UserMessage()));
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagingService.storeMessage(withAny(new Messaging()));
            times = 0;
            userMessageLogDao.create(withAny(new UserMessageLog()));
            times = 0;
        }};
    }

    @Test
    public void testSubmitDuplicateMessage(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = new UserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.ACKNOWLEDGED;

        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + DuplicateMessageException.class.getCanonicalName());
        } catch (DuplicateMessageException ex) {
            LOG.debug("DuplicateMessageException catched: " + ex.getMessage());
            assert (ex.getMessage().contains("already exists. Message identifiers must be unique"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
        }};
    }

    @Test
    public void testVerifyOriginalUserNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            result = "mycorner";

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + AccessDeniedException.class.getCanonicalName());
        } catch (AccessDeniedException ex) {
            LOG.debug("AccessDeniedException catched: " + ex.getMessage());
            assert (ex.getMessage().contains("You are not allowed to handle this message. You are authorized as [mycorner]"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext(SecurityContextHolder.getContext());
            messageIdGenerator.generateMessageId();
            times = 0;
        }};
    }


    @Test
    public void testDownloadMessageOK() throws SendMessageFault, InterruptedException, SQLException {
        //TODO

    }


}
