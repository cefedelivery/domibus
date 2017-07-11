package eu.domibus.plugin.handler;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.message.UserMessageService;
import eu.domibus.api.pmode.PModeException;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.validators.BackendMessageValidator;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.ebms3.common.model.Service;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.DuplicateMessageException;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.PModeMismatchException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.impl.SubmissionAS4Transformer;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

import javax.jms.Queue;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Federico Martini
 * @since 3.2
 * <p>
 * in the Verifications() the execution "times" is by default 1.
 */
@RunWith(JMockit.class)
public class DatabaseMessageHandlerTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DatabaseMessageHandlerTest.class);
    private static final String BACKEND = "backend";
    private static final String DEF_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String STRING_TYPE = "string";
    private static final String MESS_ID = UUID.randomUUID().toString();
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
    private MessageExchangeService messageExchangeService;

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
    BackendMessageValidator backendMessageValidator;

    @Injectable
    AuthUtils authUtils;

    @Injectable
    private UserMessageService userMessageService;


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

    @Test
    public void testSubmitMessageGreen2RedOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;

            String pModeKey = "green_gw:red_gw:testService1:TC2Leg1::pushTestcase1tc2Action";

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");
            result = messageExchangeConfiguration;

            messageExchangeService.getMessageStatus(messageExchangeConfiguration);
            result = MessageStatus.SEND_ENQUEUED;

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

            Mpc mpc = new Mpc();
            mpc.setName(Ebms3Constants.DEFAULT_MPC);

            LegConfiguration legConfiguration = new LegConfiguration();
            final Map<Party, Mpc> mpcMap = new HashMap<>();
            mpcMap.put(receiver, mpc);
            legConfiguration.setDefaultMpc(mpc);
            legConfiguration.setErrorHandling(new ErrorHandling());

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            compressionService.handleCompression(userMessage, legConfiguration);
            result = true;
        }};

        String messageId = dmh.submit(messageData, BACKEND);
        assertEquals(messageId, MESS_ID);

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            pModeProvider.getLegConfiguration(anyString);
            compressionService.handleCompression(withAny(new UserMessage()), withAny(new LegConfiguration()));
            messagingService.storeMessage(withAny(new Messaging()));
            userMessageLogDao.create(withAny(new UserMessageLog()));
            userMessageService.scheduleSending(MESS_ID);
        }};

    }

    @Test
    public void testSubmitPullMessageGreen2RedOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;

            String pModeKey = "green_gw:red_gw:testService1:TC2Leg1::pushTestcase1tc2Action";

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");
            result = messageExchangeConfiguration;

            messageExchangeService.getMessageStatus(messageExchangeConfiguration);
            result = MessageStatus.READY_TO_PULL;

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

            Mpc mpc = new Mpc();
            mpc.setName(Ebms3Constants.DEFAULT_MPC);

            LegConfiguration legConfiguration = new LegConfiguration();
            final Map<Party, Mpc> mpcMap = new HashMap<>();
            mpcMap.put(receiver, mpc);
            legConfiguration.setDefaultMpc(mpc);
            legConfiguration.setErrorHandling(new ErrorHandling());

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            compressionService.handleCompression(userMessage, legConfiguration);
            result = true;
        }};

        String messageId = dmh.submit(messageData, BACKEND);
        assertEquals(messageId, MESS_ID);

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            pModeProvider.getLegConfiguration(anyString);
            UserMessage message;
            compressionService.handleCompression(message = withCapture(), withAny(new LegConfiguration()));
            assertEquals("TC2Leg1", message.getCollaborationInfo().getAction());
            assertEquals("bdx:noprocess", message.getCollaborationInfo().getService().getValue());
            messagingService.storeMessage(withAny(new Messaging()));
            UserMessageLog userMessageLog;
            userMessageLogDao.create(userMessageLog = withCapture());
            assertEquals(MessageStatus.READY_TO_PULL, userMessageLog.getMessageStatus());
            userMessageService.scheduleSending(MESS_ID);
            times = 0;
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

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = new MessageExchangeConfiguration("","green_gw","red_gw","testService1","TC2Leg1","pushTestcase1tc2Action");;

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
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
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
            String messageId = "abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656@domibus.eu";
            userMessage.getMessageInfo().setMessageId(messageId);
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            backendMessageValidator.validateMessageId(messageId);
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "MessageId value is too long (over 255 characters)", null, null);
        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0008, mpEx.getEbms3ErrorCode());
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            times = 0;
            userMessageLogDao.getMessageStatus(MESS_ID);
            times = 0;
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
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
            String refToMessageId = "abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656abc012f4c-5a31-4759-ad9c-1d12331420656@domibus.eu";
            userMessage.getMessageInfo().setRefToMessageId(refToMessageId);
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            backendMessageValidator.validateRefToMessageId(refToMessageId);
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "RefToMessageId value is too long (over 255 characters)", refToMessageId, null);
        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(ErrorCode.EBMS_0008, mpEx.getEbms3ErrorCode());
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            times = 0;
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
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


            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = new MessageExchangeConfiguration("","green_gw","red_gw","testService1","TC2Leg1","pushTestcase1tc2Action");;

            // Here the configuration of the access point is supposed to be BLUE!
            Configuration conf = new Configuration();
            Party confParty = new Party();
            confParty.setName(BLUE);
            conf.setParty(confParty);
            pModeProvider.getConfigurationDAO().read();
            result = conf;

            backendMessageValidator.validateInitiatorParty(withAny(new Party()), withAny(new Party()));
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "The initiator party's name [" + GREEN + "] does not correspond to the access point's name [" + BLUE + "]", null, null);

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
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            backendMessageValidator.validateParties(withAny(new Party()), withAny(new Party()));
            backendMessageValidator.validateInitiatorParty(withAny(new Party()), withAny(new Party()));
            backendMessageValidator.validateResponderParty(withAny(new Party()), withAny(new Party()));
            times = 0;
            backendMessageValidator.validatePartiesRoles(withAny(new Role()), withAny(new Role()));
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
    /* Tests a submit message where from and to parties are the same. */
    public void testSubmitMessageBlue2BlueNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;

            backendMessageValidator.validateParties(withAny(new Party()), withAny(new Party()));
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "The initiator party's name is the same as the responder party's one", null, null);

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
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            backendMessageValidator.validateParties(withAny(new Party()), withAny(new Party()));

            backendMessageValidator.validateInitiatorParty(withAny(new Party()), withAny(new Party()));
            times = 0;
            backendMessageValidator.validateResponderParty(withAny(new Party()), withAny(new Party()));
            times = 0;
            backendMessageValidator.validatePartiesRoles(withAny(new Role()), withAny(new Role()));
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

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = new MessageExchangeConfiguration("","green_gw","red_gw","testService1","TC2Leg1","pushTestcase1tc2Action");;

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
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
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

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
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
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            pModeProvider.getLegConfiguration(anyString);
            times = 0;
            messagingService.storeMessage(withAny(new Messaging()));
            times = 0;
            userMessageLogDao.create(withAny(new UserMessageLog()));
            times = 0;
        }};
    }

    @Test
    public void testSubmitPullMessagePModeNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;

            String pModeKey = "green_gw:red_gw:testService1:TC2Leg1::pushTestcase1tc2Action";
            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");
            result = messageExchangeConfiguration;
            ;

            messageExchangeService.getMessageStatus(messageExchangeConfiguration);
            result = new PModeException(DomibusCoreErrorCode.DOM_003, "invalid pullprocess configuration");
        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (PModeMismatchException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(mpEx.getEbms3ErrorCode(), ErrorCode.EBMS_0010);
            assert (mpEx.getMessage().contains("invalid pullprocess configuration"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            pModeProvider.getLegConfiguration(anyString);
            messagingService.storeMessage(withAny(new Messaging()));
            userMessageLogDao.create(withAny(new UserMessageLog()));
            times = 0;
            userMessageService.scheduleSending(MESS_ID);
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
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
        }};
    }

    @Test
    public void testVerifyOriginalUserNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            authUtils.getOriginalUserFromSecurityContext();
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
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            times = 0;
        }};
    }


    @Test
    public void testSubmitMessageStoreNOk(@Injectable final Submission messageData) throws Exception {
        new Expectations() {{

            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;

            String pModeKey = "green_gw:red_gw:testService1:TC2Leg1::pushTestcase1tc2Action";
            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            result = new MessageExchangeConfiguration("","green_gw","red_gw","testService1","TC2Leg1","pushTestcase1tc2Action");;

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

            messagingService.storeMessage(new Messaging());
            result = new CompressionException("Could not store binary data for message due to IO exception", new IOException("test compression"));
        }};

        try {
            dmh.submit(messageData, BACKEND);
            Assert.fail("It should throw " + MessagingProcessingException.class.getCanonicalName());
        } catch (MessagingProcessingException mpEx) {
            LOG.debug("MessagingProcessingException catched: " + mpEx.getMessage());
            assertEquals(mpEx.getEbms3ErrorCode(), ErrorCode.EBMS_0303);
            assert (mpEx.getMessage().contains("Could not store binary data for message due to IO exception"));
        }

        new Verifications() {{
            authUtils.getOriginalUserFromSecurityContext();
            messageIdGenerator.generateMessageId();
            userMessageLogDao.getMessageStatus(MESS_ID);
            pModeProvider.findUserMessageExchangeContext(withAny(new UserMessage()), MSHRole.SENDING);
            pModeProvider.getLegConfiguration(anyString);
            compressionService.handleCompression(withAny(new UserMessage()), withAny(new LegConfiguration()));
            messagingService.storeMessage(withAny(new Messaging()));
            userMessageLogDao.create(withAny(new UserMessageLog()));
            times = 0;
        }};

    }


    public void testStoreMessageToBePulled(@Injectable final Submission messageData) throws EbMS3Exception {
        new Expectations() {{

            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            UserMessage userMessage = createUserMessage();
            transformer.transformFromSubmission(messageData);
            result = userMessage;

            messageIdGenerator.generateMessageId();
            result = MESS_ID;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.NOT_FOUND;

            String pModeKey = "green_gw:red_gw:testService1:TC2Leg1::pushTestcase1tc2Action";

            pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration("", "green_gw", "red_gw", "testService1", "TC2Leg1", "pushTestcase1tc2Action");
            result = messageExchangeConfiguration;

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

            Mpc mpc = new Mpc();
            mpc.setName(Ebms3Constants.DEFAULT_MPC);

            LegConfiguration legConfiguration = new LegConfiguration();
            final Map<Party, Mpc> mpcMap = new HashMap<>();
            mpcMap.put(receiver, mpc);
            legConfiguration.setDefaultMpc(mpc);
            legConfiguration.setErrorHandling(new ErrorHandling());

            pModeProvider.getLegConfiguration(pModeKey);
            result = legConfiguration;

            compressionService.handleCompression(userMessage, legConfiguration);
            result = true;

            messageExchangeService.getMessageStatus(messageExchangeConfiguration);
            messageExchangeConfiguration.updateStatus(MessageStatus.READY_TO_PULL);
            result= messageExchangeConfiguration;

        }};

    }
    @Test
    public void testDownloadMessageOK() throws Exception {

        final UserMessage userMessage = createUserMessage();

        final Submission submission = new Submission();
        submission.setMessageId(MESS_ID);

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";

            messagingDao.findUserMessageByMessageId(MESS_ID);
            result = userMessage;

            userMessageLogDao.findByMessageId(MESS_ID, MSHRole.RECEIVING);
            result = new UserMessageLog();

            pModeProvider.getRetentionDownloadedByMpcURI(userMessage.getMpc());
            result = 0;

            List<SignalMessage> signalMessages = new ArrayList<>();
            SignalMessage signMsg = new SignalMessage();
            signalMessages.add(signMsg);

            signalMessageDao.findSignalMessagesByRefMessageId(MESS_ID);
            result = signalMessages;

            List<String> signalMessageIds = new ArrayList<>();
            signalMessageIds.add("SignalA1");
            signalMessageIds.add("SignalA2");
            signalMessageIds.add("SignalA3");

            signalMessageDao.findSignalMessageIdsByRefMessageId(MESS_ID);
            result = signalMessageIds;

            transformer.transformFromMessaging(userMessage);
            result = submission;

        }};

        final Submission sub = dmh.downloadMessage(MESS_ID);
        Assert.assertNotNull(sub);
        Assert.assertEquals(MESS_ID, sub.getMessageId());
        Assert.assertEquals(submission, sub);

        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            userMessageLogDao.findByMessageId(MESS_ID, MSHRole.RECEIVING);
            userMessageLogDao.setMessageAsDownloaded(anyString);
            pModeProvider.getRetentionDownloadedByMpcURI(userMessage.getMpc());
            messagingDao.clearPayloadData(anyString);
            signalMessageDao.findSignalMessagesByRefMessageId(MESS_ID);
            userMessageLogDao.setMessageAsDeleted(anyString);
            signalMessageLogDao.setMessageAsDeleted(anyString);
        }};

    }

    @Test
    public void testDownloadMessageOK_RetentionNonZero() throws Exception {

        final UserMessage userMessage = createUserMessage();

        final Submission submission = new Submission();
        submission.setMessageId(MESS_ID);

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";

            messagingDao.findUserMessageByMessageId(MESS_ID);
            result = userMessage;

            userMessageLogDao.findByMessageId(MESS_ID, MSHRole.RECEIVING);
            result = new UserMessageLog();

            pModeProvider.getRetentionDownloadedByMpcURI(userMessage.getMpc());
            result = 5;

            transformer.transformFromMessaging(userMessage);
            result = submission;

        }};

        final Submission sub = dmh.downloadMessage(MESS_ID);
        Assert.assertNotNull(sub);
        Assert.assertEquals(MESS_ID, sub.getMessageId());
        Assert.assertEquals(submission, sub);

        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            userMessageLogDao.findByMessageId(MESS_ID, MSHRole.RECEIVING);
            userMessageLogDao.setMessageAsDownloaded(anyString);
            pModeProvider.getRetentionDownloadedByMpcURI(userMessage.getMpc());
            messagingDao.clearPayloadData(anyString);
            times = 0;
            signalMessageDao.findSignalMessagesByRefMessageId(MESS_ID);
            times = 0;
            userMessageLogDao.setMessageAsDeleted(anyString);
            times = 0;
            signalMessageLogDao.setMessageAsDeleted(anyString);
            times = 0;
        }};

    }

    @Test
    public void testDownloadMessageAuthUserNok() throws Exception {

        final UserMessage userMessage = createUserMessage();

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            messagingDao.findUserMessageByMessageId(MESS_ID);
            result = userMessage;
        }};

        try {
            dmh.downloadMessage(MESS_ID);
            Assert.fail("It should throw " + AccessDeniedException.class.getCanonicalName());
        } catch (AccessDeniedException adEx) {
            LOG.debug("Expected :", adEx);
            assert (adEx.getMessage().contains("You are not allowed to handle this message. You are authorized as"));
        }

        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            authUtils.getOriginalUserFromSecurityContext();
            messagingDao.findUserMessageByMessageId(MESS_ID);
        }};

    }

    @Test
    public void testDownloadMessageNoMsgFound() throws Exception {

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            messagingDao.findUserMessageByMessageId(MESS_ID);
            result = new NoResultException("No entry found");
        }};

        try {
            dmh.downloadMessage(MESS_ID);
            Assert.fail("It should throw " + MessageNotFoundException.class.getCanonicalName());
        } catch (MessageNotFoundException mnfEx) {
            LOG.debug("Expected :", mnfEx);
            assert (mnfEx.getMessage().contains("was not found"));
        }

        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            authUtils.getOriginalUserFromSecurityContext();
            messagingDao.findUserMessageByMessageId(MESS_ID);
            userMessageLogDao.findByMessageId(MESS_ID, MSHRole.RECEIVING);
            times = 0;
        }};

    }

    @Test
    public void testDownloadMessageNoLogMsgFound() throws Exception {

        new Expectations() {{
            authUtils.isUnsecureLoginAllowed();
            result = false;

            authUtils.getOriginalUserFromSecurityContext();
            result = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";

            messagingDao.findUserMessageByMessageId(MESS_ID);
            result = new UserMessage();

            userMessageLogDao.findByMessageId(MESS_ID, MSHRole.RECEIVING);
            result = null;
        }};

        try {
            dmh.downloadMessage(MESS_ID);
            Assert.fail("It should throw " + MessageNotFoundException.class.getCanonicalName());
        } catch (MessageNotFoundException mnfEx) {
            LOG.debug("Expected :", mnfEx);
            assert (mnfEx.getMessage().contains("was not found"));
        }

        new Verifications() {{
            authUtils.hasUserOrAdminRole();
            authUtils.getOriginalUserFromSecurityContext();
            messagingDao.findUserMessageByMessageId(MESS_ID);
            userMessageLogDao.findByMessageId(MESS_ID, MSHRole.RECEIVING);
        }};

    }

    @Test
    public void testGetMessageStatusOk() throws Exception {
        new Expectations() {{

            authUtils.isUnsecureLoginAllowed();
            result = false;

            userMessageLogDao.getMessageStatus(MESS_ID);
            result = MessageStatus.ACKNOWLEDGED;

        }};

        final MessageStatus msgStatus = dmh.getMessageStatus(MESS_ID);

        new Verifications() {{
            authUtils.hasAdminRole();
            userMessageLogDao.getMessageStatus(MESS_ID);
            msgStatus.equals(MessageStatus.ACKNOWLEDGED);
        }};

    }

    @Test
    public void testGetErrorsForMessageOk() throws Exception {
        new Expectations() {{

            authUtils.isUnsecureLoginAllowed();
            result = false;

            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0008, "MessageId value is too long (over 255 characters)", MESS_ID, null);
            List<ErrorResult> list = new ArrayList<>();
            list.add(new ErrorLogEntry(ex));

            errorLogDao.getErrorsForMessage(MESS_ID);
            result = list;

        }};

        final List<? extends ErrorResult> results = dmh.getErrorsForMessage(MESS_ID);


        new Verifications() {{
            authUtils.hasAdminRole();
            errorLogDao.getErrorsForMessage(MESS_ID);
            Assert.assertNotNull(results);
            ErrorResult errRes = results.iterator().next();
            Assert.assertEquals(errRes.getErrorCode(), ErrorCode.EBMS_0008);
        }};

    }

}
