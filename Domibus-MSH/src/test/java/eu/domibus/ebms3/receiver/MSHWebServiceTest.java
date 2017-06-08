package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.*;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.ReplyPattern;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.common.validators.PayloadProfileValidator;
import eu.domibus.common.validators.PropertyProfileValidator;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.plugin.validation.SubmissionValidationException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Arun Raj
 * @since 3.3
 */

@RunWith(JMockit.class)
public class MSHWebServiceTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHWebServiceTest.class);
    private static final String TEST_RESOURCES_DIR = "./src/test/resources";
    private static final String VALID_PMODE_CONFIG_URI = "SamplePModes/domibus-configuration-valid.xml";
    private static final String LEG_NO_SECNO_SEC_ACTION = "pushNoSecnoSecAction";
    private static final String PUSH_TESTCASE1_TC1ACTION = "pushTestcase1tc1Action";
    private static final String STRING_TYPE = "string";
    private static final String DEF_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";
    private static final String FINAL_RECEIPIENT_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    RawEnvelopeLogDao rawEnvelopeLogDao;

    @Injectable
    MessagingService messagingService;

    @Injectable
    SignalMessageDao signalMessageDao;

    @Injectable
    SignalMessageLogDao signalMessageLogDao;

    @Injectable
    MessageFactory messageFactory;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Injectable
    JAXBContext jaxbContext;

    @Injectable
    TransformerFactory transformerFactory;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    TimestampDateFormatter timestampDateFormatter;

    @Injectable
    CompressionService compressionService;

    @Injectable
    MessageIdGenerator messageIdGenerator;

    @Injectable
    PayloadProfileValidator payloadProfileValidator;

    @Injectable
    PropertyProfileValidator propertyProfileValidator;

    @Injectable
    CertificateService certificateService;

    @Injectable
    SOAPMessage soapRequestMessage;

    @Injectable
    SOAPMessage soapResponseMessage;

    @Tested
    MSHWebservice mshWebservice;

    /**
     * Happy flow unit testing with actual data
     *
     * @throws SOAPException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws JAXBException
     * @throws EbMS3Exception
     * @throws TransformerException
     */
    @Test
    public void testInvoke_tc1Process_HappyFlow() throws SOAPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, TransformerException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";
        final Configuration configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        final LegConfiguration legConfiguration = getLegFromConfiguration(configuration, PUSH_TESTCASE1_TC1ACTION);
        final Messaging messaging = createDummyRequestMessaging();
        final UserMessage userMessage = messaging.getUserMessage();
        final Party receiverParty = getPartyFromConfiguration(configuration, RED);

        new Expectations(mshWebservice) {{
            soapRequestMessage.getProperty(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY);
            result = pmodeKey;

            pModeProvider.getLegConfiguration(withSubstring(PUSH_TESTCASE1_TC1ACTION));
            result = legConfiguration;

            mshWebservice.getMessaging(withAny(soapRequestMessage));
            result = messaging;

            mshWebservice.checkDuplicate(messaging);
            result = false;

            mshWebservice.handlePayloads(soapRequestMessage, userMessage);
            result = any;

            compressionService.handleDecompression(userMessage, legConfiguration);
            result = true;

            pModeProvider.getReceiverParty(pmodeKey);
            result = receiverParty;

            mshWebservice.generateReceipt(withAny(soapRequestMessage), legConfiguration, anyBoolean);
            result = soapResponseMessage;
        }};

        mshWebservice.invoke(soapRequestMessage);

        new Verifications() {{
            mshWebservice.checkCharset(messaging);
            mshWebservice.checkPingMessage(messaging.getUserMessage());
            mshWebservice.checkDuplicate(messaging);
            mshWebservice.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, messaging);
            backendNotificationService.notifyMessageReceived(messaging.getUserMessage());
            mshWebservice.generateReceipt(withAny(soapRequestMessage), legConfiguration, anyBoolean);
            backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), (ErrorResult) any);
            times = 0;
        }};
    }

    @Test
    public void testInvoke_DuplicateMessage(@Injectable final LegConfiguration legConfiguration, @Injectable final Messaging messaging, @Injectable final UserMessage userMessage) throws SOAPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, TransformerException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(mshWebservice) {{
            soapRequestMessage.getProperty(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY);
            result = pmodeKey;

            pModeProvider.getLegConfiguration(withSubstring(PUSH_TESTCASE1_TC1ACTION));
            result = legConfiguration;

            mshWebservice.getMessaging(withAny(soapRequestMessage));
            result = messaging;

            messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "TestMessage123";

            mshWebservice.checkCharset(withAny(messaging));
            result = any;

            mshWebservice.checkPingMessage(withAny(userMessage));
            result = false;

            legConfiguration.getReceptionAwareness().getDuplicateDetection();
            result = true;

            mshWebservice.checkDuplicate(withAny(messaging));
            result = true;

            mshWebservice.generateReceipt(withAny(soapRequestMessage), legConfiguration, anyBoolean);
            result = soapResponseMessage;
        }};

        mshWebservice.invoke(soapRequestMessage);

        new Verifications() {{
            mshWebservice.checkCharset(messaging);
            mshWebservice.checkPingMessage(messaging.getUserMessage());
            mshWebservice.checkDuplicate(messaging);
            mshWebservice.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, messaging);
            times = 0;
            backendNotificationService.notifyMessageReceived(messaging.getUserMessage());
            times = 0;
            mshWebservice.generateReceipt(withAny(soapRequestMessage), legConfiguration, anyBoolean);
            backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), (ErrorResult) any);
            times = 0;
        }};
    }

    @Test
    public void testInvoke_PingMessage(@Injectable final LegConfiguration legConfiguration, @Injectable final Messaging messaging, @Injectable final UserMessage userMessage) throws SOAPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, TransformerException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(mshWebservice) {{
            soapRequestMessage.getProperty(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY);
            result = pmodeKey;

            pModeProvider.getLegConfiguration(withSubstring(PUSH_TESTCASE1_TC1ACTION));
            result = legConfiguration;

            mshWebservice.getMessaging(withAny(soapRequestMessage));
            result = messaging;

            messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "TestMessage123";

            mshWebservice.checkCharset(withAny(messaging));
            result = any;

            mshWebservice.checkPingMessage(withAny(userMessage));
            result = true;

            legConfiguration.getReceptionAwareness().getDuplicateDetection();
            result = true;

            mshWebservice.checkDuplicate(withAny(messaging));
            result = false;

            mshWebservice.generateReceipt(withAny(soapRequestMessage), legConfiguration, anyBoolean);
            result = soapResponseMessage;
        }};

        mshWebservice.invoke(soapRequestMessage);

        new Verifications() {{
            mshWebservice.checkCharset(messaging);
            mshWebservice.checkPingMessage(messaging.getUserMessage());
            mshWebservice.checkDuplicate(messaging);
            mshWebservice.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, messaging);
            times = 0;
            backendNotificationService.notifyMessageReceived(messaging.getUserMessage());
            times = 0;
            mshWebservice.generateReceipt(withAny(soapRequestMessage), legConfiguration, anyBoolean);
            backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), (ErrorResult) any);
            times = 0;
        }};
    }

    @Test
    public void testInvoke_ErrorInNotifyingIncomingMessage(@Injectable final LegConfiguration legConfiguration, @Injectable final Messaging messaging, @Injectable final UserMessage userMessage) throws SOAPException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, EbMS3Exception, TransformerException {

        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(mshWebservice) {{
            soapRequestMessage.getProperty(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY);
            result = pmodeKey;

            pModeProvider.getLegConfiguration(withSubstring(PUSH_TESTCASE1_TC1ACTION));
            result = legConfiguration;

            mshWebservice.getMessaging(withAny(soapRequestMessage));
            result = messaging;

            messaging.getUserMessage().getMessageInfo().getMessageId();
            result = "TestMessage123";

            mshWebservice.checkPingMessage(withAny(userMessage));
            result = false;

            legConfiguration.getReceptionAwareness().getDuplicateDetection();
            result = true;

            mshWebservice.checkDuplicate(withAny(messaging));
            result = false;

            mshWebservice.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, messaging);
            result = any;

            backendNotificationService.notifyMessageReceived(withAny(userMessage));
            result = new SubmissionValidationException("Error while submitting the message!!");

            legConfiguration.getErrorHandling().isBusinessErrorNotifyConsumer();
            result = true;

        }};

        try {
            mshWebservice.invoke(soapRequestMessage);
        } catch (Exception e) {
            Assert.assertTrue("Expecting Webservice exception!", e instanceof WebServiceException);
        }

        new Verifications() {{
            backendNotificationService.notifyMessageReceived(messaging.getUserMessage());
            mshWebservice.generateReceipt(withAny(soapRequestMessage), legConfiguration, anyBoolean);
            times = 0;
            backendNotificationService.notifyMessageReceivedFailure(messaging.getUserMessage(), (ErrorResult) any);
        }};
    }

    @Test
    public void testCheckCharset_HappyFlow() throws EbMS3Exception {
        final Messaging messaging = new Messaging();
        UserMessage userMessage = new UserMessage();
        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = new PartInfo();

        PartProperties partProperties = new PartProperties();
        partProperties.getProperties().add(createProperty("MimeType", "text/xml", STRING_TYPE));
        partInfo.setPartProperties(partProperties);

        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        messaging.setUserMessage(userMessage);

        try {
            mshWebservice.checkCharset(messaging);
        } catch (Exception e) {
            Assert.fail("No exception was expected!! Should have been handled successfully");
        }
    }

    @Test
    public void testCheckCharset_InvalidCharset() {
        final Messaging messaging = new Messaging();
        UserMessage userMessage = new UserMessage();
        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = new PartInfo();

        PartProperties partProperties = new PartProperties();
        partProperties.getProperties().add(createProperty("CharacterSet", "!#InvalidCharSet", STRING_TYPE));
        partInfo.setPartProperties(partProperties);

        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        messaging.setUserMessage(userMessage);

        try {
            mshWebservice.checkCharset(messaging);
            Assert.fail("EBMS3Exception was expected!!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0003, e.getErrorCode());
        }
    }

    @Test
    public void testCheckDuplicate(@Injectable final UserMessageLog userMessageLog) {
        new Expectations() {{
            userMessageLogDao.findByMessageId(withSubstring("1234"), MSHRole.RECEIVING);
            result = userMessageLog;

            userMessageLogDao.findByMessageId(anyString, MSHRole.RECEIVING);
            result = null;
        }};
        Messaging messaging1 = new Messaging();
        UserMessage userMessage1 = new UserMessage();
        MessageInfo messageInfo1 = new MessageInfo();
        messageInfo1.setMessageId("1234");
        userMessage1.setMessageInfo(messageInfo1);
        messaging1.setUserMessage(userMessage1);
        Assert.assertTrue("Expecting match in duplicate check", mshWebservice.checkDuplicate(messaging1));

        Messaging messaging2 = new Messaging();
        UserMessage userMessage2 = new UserMessage();
        MessageInfo messageInfo2 = new MessageInfo();
        messageInfo2.setMessageId("4567");
        userMessage2.setMessageInfo(messageInfo2);
        messaging2.setUserMessage(userMessage2);
        Assert.assertFalse("Expecting not duplicate result", mshWebservice.checkDuplicate(messaging2));
    }

    @Test
    public void testCheckPingMessage() {

        UserMessage userMessage = createSampleUserMessage();
        Assert.assertFalse("Expecting false in test for ping message as valid data message is supplied ", mshWebservice.checkPingMessage(userMessage));


        userMessage.getCollaborationInfo().getService().setValue(Ebms3Constants.TEST_SERVICE);
        userMessage.getCollaborationInfo().setAction(Ebms3Constants.TEST_ACTION);
        Assert.assertTrue("Expecting true for Check Ping Message with modified data", mshWebservice.checkPingMessage(userMessage));
    }

    @Test
    public void testGenerateReceipt_WithReliabilityAndResponseRequired() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException, SOAPException {

        final Configuration configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        final LegConfiguration legConfiguration = getLegFromConfiguration(configuration, PUSH_TESTCASE1_TC1ACTION);

        new Expectations(mshWebservice) {{
            messageFactory.createMessage();
            result = soapResponseMessage;

            //Expecting that the saveResponse call will be invoked without any exception.
            mshWebservice.saveResponse(withAny(soapResponseMessage));
            result = any;
        }};

        try {
            mshWebservice.generateReceipt(soapRequestMessage, legConfiguration, false);
        } catch (Exception e) {
            Assert.fail("No exception was expected with valid configuration input !!!");
        }

        new Verifications() {{
            mshWebservice.saveResponse(withAny(soapResponseMessage));
            times = 1;
        }};
    }

    @Test
    public void testGenerateReceipt_NoReliability(@Injectable final LegConfiguration legConfiguration) {
        new Expectations(mshWebservice) {{
            legConfiguration.getReliability();
            result = null;
        }};

        try {
            mshWebservice.generateReceipt(soapRequestMessage, legConfiguration, false);
        } catch (Exception e) {
            Assert.fail("No exception was expected with valid configuration input !!!");
        }

        new Verifications() {{
            //verify that saveResponse is not invoked
            mshWebservice.saveResponse(withAny(soapResponseMessage));
            times = 0;
        }};
    }

    @Test
    public void testGenerateReceipt_NoResponse(@Injectable final LegConfiguration legConfiguration) {
        new Expectations(mshWebservice) {{
            legConfiguration.getReliability();
            result = "AS4Reliability";

            legConfiguration.getReliability().getReplyPattern();
            result = ReplyPattern.CALLBACK;
        }};

        try {
            mshWebservice.generateReceipt(soapRequestMessage, legConfiguration, false);
        } catch (Exception e) {
            Assert.fail("No exception was expected with valid configuration input !!!");
        }

        new Verifications() {{
            mshWebservice.saveResponse(withAny(soapResponseMessage));
            times = 0;
        }};
    }


    @Test
    public void testGenerateReceipt_TransformException(@Injectable final LegConfiguration legConfiguration, @Injectable final Source messageToReceiptTransform, @Injectable final Transformer transformer, @Injectable final DOMResult domResult) throws SOAPException, TransformerException {
        new Expectations(mshWebservice) {{
            legConfiguration.getReliability();
            result = "AS4Reliability";

            legConfiguration.getReliability().getReplyPattern();
            result = ReplyPattern.RESPONSE;

            messageFactory.createMessage();
            result = soapResponseMessage;

            transformerFactory.newTransformer(withAny(messageToReceiptTransform));
            result = transformer;

            transformer.transform(withAny(messageToReceiptTransform), withAny(domResult));
            result = new TransformerException("TEST Transformer Exception");
        }};

        try {
            mshWebservice.generateReceipt(soapRequestMessage, legConfiguration, false);
            Assert.fail("Expected Transformer exception to be raised !!!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0201, e.getErrorCode());
        }

        new Verifications() {{
            mshWebservice.saveResponse(withAny(soapResponseMessage));
            times = 0;
        }};
    }


    @Test
    public void testSaveResponse(@Injectable final SOAPHeader soapHeader, @Injectable final Iterator messagingIterator, @Injectable final Node node, @Injectable final Messaging receiptMessage) throws SOAPException, ParserConfigurationException, JAXBException, SAXException, IOException {

        final Messaging responseMessaging = createValidSampleResponseMessaging();
        final SignalMessage responseSignalMessage = responseMessaging.getSignalMessage();
        new Expectations() {{
            soapResponseMessage.getSOAPHeader();
            result = soapHeader;

            soapHeader.getChildElements(ObjectFactory._Messaging_QNAME);
            result = messagingIterator;

            messagingIterator.next();
            result = node;

            jaxbContext.createUnmarshaller().unmarshal(node, Messaging.class).getValue();
            result = responseMessaging;

            messagingDao.findMessageByMessageId(responseSignalMessage.getMessageInfo().getRefToMessageId());
            result = receiptMessage;
        }};

        mshWebservice.saveResponse(soapResponseMessage);

        new Verifications() {{
            signalMessageDao.create(responseSignalMessage);
            times = 1;

            messagingDao.update(receiptMessage);
            times = 1;
        }};
    }


    @Test
    public void testSaveResponse_SuppressedExceptionFlow(@Injectable final SOAPHeader soapHeader, @Injectable final Messaging receiptMessage) throws SOAPException, ParserConfigurationException, JAXBException, SAXException, IOException {

        final Messaging responseMessaging = createValidSampleResponseMessaging();
        final SignalMessage responseSignalMessage = responseMessaging.getSignalMessage();
        new Expectations() {{
            soapResponseMessage.getSOAPHeader();
            result = soapHeader;

            soapHeader.getChildElements(ObjectFactory._Messaging_QNAME);
            result = new SOAPException();
        }};

        try {
            mshWebservice.saveResponse(soapResponseMessage);
        } catch (Exception e) {
            Assert.fail("No exception is expected to be raised.");
        }

        new Verifications() {{
            signalMessageDao.create(responseSignalMessage);
            times = 0;

            messagingDao.update(receiptMessage);
            times = 0;
        }};
    }

    @Test
    public void testSaveResponse_DBWriteExceptionFlow(@Injectable final SOAPHeader soapHeader, @Injectable final Iterator messagingIterator, @Injectable final Node node, @Injectable final Messaging receiptMessage, @Injectable final SignalMessageLog signalMessageLog) throws SOAPException, ParserConfigurationException, JAXBException, SAXException, IOException {

        final Messaging responseMessaging = createValidSampleResponseMessaging();
        final SignalMessage responseSignalMessage = responseMessaging.getSignalMessage();
        new Expectations() {{
            soapResponseMessage.getSOAPHeader();
            result = soapHeader;

            soapHeader.getChildElements(ObjectFactory._Messaging_QNAME);
            result = messagingIterator;

            messagingIterator.next();
            result = node;

            jaxbContext.createUnmarshaller().unmarshal(node, Messaging.class).getValue();
            result = responseMessaging;

            messagingDao.findMessageByMessageId(responseSignalMessage.getMessageInfo().getRefToMessageId());
            result = receiptMessage;

            signalMessageLogDao.create(withAny(signalMessageLog));
            result = new RuntimeException();
        }};

        try {
            mshWebservice.saveResponse(soapResponseMessage);
            Assert.fail("Expected failure propagation during DB commit failure!");
        } catch (Exception e) {
            Assert.assertTrue("Expected Runtime exception mocked!", e instanceof RuntimeException);
        }

        new Verifications() {{
            signalMessageDao.create(responseSignalMessage);
            times = 1;

            messagingDao.update(receiptMessage);
            times = 1;
        }};
    }

    /**
     * For the Happy Flow the Unit test with full data is happening with the test - testInvoke_tc1Process().
     * This test is using mock objects.
     *
     * @param legConfiguration
     * @param messaging
     * @param userMessage
     * @param receiverParty
     * @param userMessageLog
     * @throws EbMS3Exception
     * @throws TransformerException
     * @throws SOAPException
     * @throws JAXBException
     */
    @Test
    public void testPersistReceivedMessage_HappyFlow(@Injectable final LegConfiguration legConfiguration, @Injectable final Messaging messaging, @Injectable final UserMessage userMessage, @Injectable final Party receiverParty, @Injectable final UserMessageLog userMessageLog) throws EbMS3Exception, TransformerException, SOAPException, JAXBException {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(mshWebservice) {{
            messaging.getUserMessage();
            result = userMessage;

            compressionService.handleDecompression(userMessage, legConfiguration);
            result = true;

            pModeProvider.getReceiverParty(pmodeKey);
            result = receiverParty;

            userMessage.getMessageInfo().getMessageId();
            result = "TestMessageId123";

        }};
        mshWebservice.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, messaging);

        new Verifications() {{
            mshWebservice.handlePayloads(soapRequestMessage, userMessage);
            payloadProfileValidator.validate(messaging, pmodeKey);
            propertyProfileValidator.validate(messaging, pmodeKey);
            messagingService.storeMessage(messaging);
            userMessageLogDao.create(withAny(userMessageLog));
        }};
    }

    @Test
    public void testPersistReceivedMessage_ValidationException(@Injectable final LegConfiguration legConfiguration, @Injectable final Messaging messaging, @Injectable final UserMessage userMessage, @Injectable final UserMessageLog userMessageLog) throws EbMS3Exception, TransformerException, SOAPException, JAXBException {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(mshWebservice) {{
            messaging.getUserMessage();
            result = userMessage;

            compressionService.handleDecompression(userMessage, legConfiguration);
            result = true;

            propertyProfileValidator.validate(messaging, pmodeKey);
            result = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Property missing exception", "Message Id", null);
        }};

        try {
            mshWebservice.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, messaging);
        } catch (Exception e) {
            Assert.assertTrue("Expecting Ebms3 exception", e instanceof EbMS3Exception);
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0010, ((EbMS3Exception) e).getErrorCode());
        }

        new Verifications() {{
            mshWebservice.handlePayloads(soapRequestMessage, userMessage);
            payloadProfileValidator.validate(messaging, pmodeKey);
            propertyProfileValidator.validate(messaging, pmodeKey);
            messagingService.storeMessage(messaging);
            times = 0;
            userMessageLogDao.create(withAny(userMessageLog));
            times = 0;
        }};
    }

    @Test
    public void testPersistReceivedMessage_CompressionError(@Injectable final LegConfiguration legConfiguration, @Injectable final Messaging messaging, @Injectable final UserMessage userMessage, @Injectable final Party receiverParty, @Injectable final UserMessageLog userMessageLog) throws EbMS3Exception, TransformerException, SOAPException, JAXBException {
        final String pmodeKey = "blue_gw:red_gw:testService1:tc1Action:OAE:pushTestcase1tc1Action";

        new Expectations(mshWebservice) {{
            messaging.getUserMessage();
            result = userMessage;

            compressionService.handleDecompression(userMessage, legConfiguration);
            result = true;

            messagingService.storeMessage(messaging);
            result = new CompressionException("Could not store binary data for message ", null);

            userMessage.getMessageInfo().getMessageId();
            result = "TestMessageId123";
        }};
        try {
            mshWebservice.persistReceivedMessage(soapRequestMessage, legConfiguration, pmodeKey, messaging);
            Assert.fail("Exception for compression failure expected!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0303, e.getErrorCode());
        }

        new Verifications() {{
            mshWebservice.handlePayloads(soapRequestMessage, userMessage);
            payloadProfileValidator.validate(messaging, pmodeKey);
            propertyProfileValidator.validate(messaging, pmodeKey);
            messagingService.storeMessage(messaging);
            userMessageLogDao.create(withAny(userMessageLog));
            times = 0;
        }};
    }

    @Test
    public void testGetFinalRecipientName() {
        final UserMessage userMessage = createSampleUserMessage();
        Assert.assertEquals(FINAL_RECEIPIENT_VALUE, mshWebservice.getFinalRecipientName(userMessage));
    }

    @Test
    public void test_HandlePayLoads_HappyFlowUsingCID(@Injectable final UserMessage userMessage, @Injectable final AttachmentPart attachmentPart1, @Injectable final AttachmentPart attachmentPart2) {

        final PartInfo partInfo = new PartInfo();
        partInfo.setHref("cid:message");

        PartProperties partProperties = new PartProperties();
        Property property1 = new Property();
        property1.setName("MimeType");
        property1.setValue("text/xml");

        partProperties.getProperties().add(property1);
        partInfo.setPartProperties(partProperties);

        List<AttachmentPart> attachmentPartList = new ArrayList<>();
        attachmentPartList.add(attachmentPart1);
        attachmentPartList.add(attachmentPart2);
        final Iterator<AttachmentPart> attachmentPartIterator = attachmentPartList.iterator();

        new Expectations() {{
            userMessage.getPayloadInfo().getPartInfo();
            result = partInfo;

            soapRequestMessage.getAttachments();
            result = attachmentPartIterator;

            attachmentPart1.getContentId();
            result = "AnotherContentID";

            attachmentPart2.getContentId();
            result = "message";
        }};

        try {
            mshWebservice.handlePayloads(soapRequestMessage, userMessage);
            Assert.assertNotNull(partInfo.getPayloadDatahandler());
            Assert.assertEquals(attachmentPart2.getDataHandler(), partInfo.getPayloadDatahandler());
        } catch (EbMS3Exception | SOAPException | TransformerException e) {
            Assert.fail("No Errors expected in happy flow!");
        }
    }

    @Test
    public void test_HandlePayLoads_HappyFlowUsingEmptyCID(@Injectable final UserMessage userMessage, @Injectable final Node bodyContent) throws SOAPException, TransformerConfigurationException {
        final PartInfo partInfo = new PartInfo();
        partInfo.setHref("");

        PartProperties partProperties = new PartProperties();
        Property property1 = new Property();
        property1.setName("MimeType");
        property1.setValue("text/xml");

        partProperties.getProperties().add(property1);
        partInfo.setPartProperties(partProperties);

        List<Node> bodyContentNodeList = new ArrayList<>();
        bodyContentNodeList.add(bodyContent);
        final Iterator<Node> bodyContentNodeIterator = bodyContentNodeList.iterator();

        new Expectations() {{
            userMessage.getPayloadInfo().getPartInfo();
            result = partInfo;

            soapRequestMessage.getSOAPBody().getChildElements();
            result = bodyContentNodeIterator;
        }};

        try {
            mshWebservice.handlePayloads(soapRequestMessage, userMessage);
            Assert.assertNotNull(partInfo.getPayloadDatahandler());
        } catch (EbMS3Exception | SOAPException | TransformerException e) {
            Assert.fail("No Errors expected in happy flow!");
        }
    }

    /**
     * A single message having multiple PartInfo's with no or special cid.
     *
     * @param userMessage
     * @param bodyContent1
     * @throws SOAPException
     * @throws TransformerException
     */
    @Test
    public void test_HandlePayLoads_NullCIDMultiplePartInfo(@Injectable final UserMessage userMessage, @Injectable final Node bodyContent1) throws SOAPException, TransformerException {

        PartInfo partInfo1 = new PartInfo();
        partInfo1.setHref("");
        PartProperties partProperties = new PartProperties();
        Property property1 = new Property();
        property1.setName("MimeType");
        property1.setValue("text/xml");
        partProperties.getProperties().add(property1);
        partInfo1.setPartProperties(partProperties);

        PartInfo partInfo2 = new PartInfo();
        partInfo1.setHref("#1234");
        PartProperties partProperties2 = new PartProperties();
        Property property2 = new Property();
        property2.setName("MimeType");
        property2.setValue("text/xml");
        partProperties2.getProperties().add(property2);
        partInfo2.setPartProperties(partProperties2);

        final PayloadInfo payloadInfo = new PayloadInfo();
        payloadInfo.getPartInfo().add(partInfo1);
        payloadInfo.getPartInfo().add(partInfo2);

        List<Node> bodyContentNodeList = new ArrayList<>();
        bodyContentNodeList.add(bodyContent1);
        final Iterator<Node> bodyContentNodeIterator = bodyContentNodeList.iterator();

        new Expectations() {{
            userMessage.getPayloadInfo();
            result = payloadInfo;

            soapRequestMessage.getSOAPBody().getChildElements();
            result = bodyContentNodeIterator;
        }};

        try {
            mshWebservice.handlePayloads(soapRequestMessage, userMessage);
            Assert.fail("Expecting error that - More than one Partinfo referencing the soap body found!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0003, e.getErrorCode());
        }
    }

    @Test
    public void test_HandlePayLoads_NoPayloadFound(@Injectable final UserMessage userMessage, @Injectable final AttachmentPart attachmentPart1, @Injectable final AttachmentPart attachmentPart2) {

        final PartInfo partInfo = new PartInfo();
        partInfo.setHref("cid:message");

        PartProperties partProperties = new PartProperties();
        Property property1 = new Property();
        property1.setName("MimeType");
        property1.setValue("text/xml");

        partProperties.getProperties().add(property1);
        partInfo.setPartProperties(partProperties);

        List<AttachmentPart> attachmentPartList = new ArrayList<>();
        attachmentPartList.add(attachmentPart1);
        attachmentPartList.add(attachmentPart2);
        final Iterator<AttachmentPart> attachmentPartIterator = attachmentPartList.iterator();

        new Expectations() {{
            userMessage.getPayloadInfo().getPartInfo();
            result = partInfo;

            soapRequestMessage.getAttachments();
            result = attachmentPartIterator;

            attachmentPart1.getContentId();
            result = "AnotherContentID";

            attachmentPart2.getContentId();
            result = "message123";
        }};

        try {
            mshWebservice.handlePayloads(soapRequestMessage, userMessage);
            Assert.fail("Expected Ebms3 exception that no matching payload was found!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0011, e.getErrorCode());
        } catch (SOAPException | TransformerException e) {
            Assert.fail("Expected Ebms3 exception that no matching payload was found!");
        }

    }

    /**
     * Unit testing with actual data.
     *
     * @param soapHeader
     * @param soapChildElementsIterator
     * @param messagingXml
     * @throws JAXBException
     * @throws SOAPException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    @Test
    public void testGetMessaging(@Injectable final SOAPHeader soapHeader, @Injectable final Iterator soapChildElementsIterator, @Injectable final Node messagingXml) throws JAXBException, SOAPException, ParserConfigurationException, IOException, SAXException {

        File validRequestFile = new File(TEST_RESOURCES_DIR + "/dataset/as4/blue2redGoodMessage.xml");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document responseFileDocument = documentBuilder.parse(validRequestFile);
        final Node messagingNode = responseFileDocument.getElementsByTagName("ns:Messaging").item(0);

        new Expectations() {{
            soapRequestMessage.getSOAPHeader();
            result = soapHeader;

            soapHeader.getChildElements(ObjectFactory._Messaging_QNAME);
            result = soapChildElementsIterator;

            soapChildElementsIterator.next();
            result = messagingNode;

            jaxbContext.createUnmarshaller();
            result = JAXBContext.newInstance(Messaging.class).createUnmarshaller();
        }};

        Assert.assertEquals(JAXBContext.newInstance(Messaging.class).createUnmarshaller().unmarshal(messagingNode, Messaging.class).getValue(), mshWebservice.getMessaging(soapRequestMessage));
    }


    public Configuration loadSamplePModeConfiguration(String samplePModeFileRelativeURI) throws JAXBException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LOG.debug("Inside sample PMode configuration");
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream(samplePModeFileRelativeURI);
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Configuration configuration = (Configuration) unmarshaller.unmarshal(xmlStream);
        Method m = configuration.getClass().getDeclaredMethod("preparePersist", null);
        m.setAccessible(true);
        m.invoke(configuration);

        return configuration;
    }

    public LegConfiguration getLegFromConfiguration(Configuration configuration, String legName) {
        LegConfiguration result = null;
        for (LegConfiguration legConfiguration1 : configuration.getBusinessProcesses().getLegConfigurations()) {
            if (StringUtils.equalsIgnoreCase(legName, legConfiguration1.getName())) {
                result = legConfiguration1;
            }
        }
        return result;
    }

    public Party getPartyFromConfiguration(Configuration configuration, String partyName) {
        Party result = null;
        for (Party party : configuration.getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(partyName, party.getName())) {
                result = party;
            }
        }
        return result;
    }

    protected Messaging createDummyRequestMessaging() {
        Messaging messaging = new ObjectFactory().createMessaging();
        messaging.setUserMessage(createSampleUserMessage());
        messaging.getUserMessage().getMessageInfo().setMessageId("1234");
        return messaging;
    }

    protected UserMessage createSampleUserMessage() {
        UserMessage userMessage = new UserMessage();
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setAction("TC1Leg1");
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
        messageProperties.getProperty().add(createProperty("finalRecipient", FINAL_RECEIPIENT_VALUE, STRING_TYPE));
        userMessage.setMessageProperties(messageProperties);

        PartyInfo partyInfo = new PartyInfo();

        From from = new From();
        from.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");

        PartyId sender = new PartyId();
        sender.setValue(BLUE);
        sender.setType(DEF_PARTY_TYPE);
        from.getPartyId().add(sender);
        partyInfo.setFrom(from);

        To to = new To();
        to.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");

        PartyId receiver = new PartyId();
        receiver.setValue(RED);
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

    protected Property createProperty(String name, String value, String type) {
        Property aProperty = new Property();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(type);
        return aProperty;
    }


    public Messaging createValidSampleResponseMessaging() throws ParserConfigurationException, IOException, SAXException, JAXBException {
        File validAS4ResponseFile = new File(TEST_RESOURCES_DIR + "/dataset/as4/validAS4Response.xml");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document responseFileDocument = documentBuilder.parse(validAS4ResponseFile);
        Node messagingNode = responseFileDocument.getElementsByTagName("eb3:Messaging").item(0);

        Messaging messaging = JAXBContext.newInstance(Messaging.class).createUnmarshaller().unmarshal(messagingNode, Messaging.class).getValue();
        return messaging;
    }
}
