package eu.domibus.common.validators;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Role;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 @author Arun Raj
 @since 3.3
 */

@RunWith(JMockit.class)
public class BackendMessageValidatorTest {

//    private static final String DOMIBUS_CONFIGURATION_FILE = "domibus-configuration.xml";
    private static final String MESSAGE_ID_PATTERN = "^[\\x20-\\x7E]*$";
    private static final String RED = "red_gw";
    private static final String BLUE = "blue_gw";
    private static final String INITIATOR_ROLE = "defaultInitiatorRole";
    private static final String RESPONDER_ROLE = "defaultResponderRole";

    @Injectable
    Properties domibusProperties;

    @Tested
    BackendMessageValidator backendMessageValidatorObj;


    @Test
    public void validateMessageId() throws Exception {

        new Expectations() {{
            domibusProperties.getProperty(BackendMessageValidator.KEY_MESSAGEID_PATTERN);
            result = MESSAGE_ID_PATTERN;

        }};

        /*Happy Flow No error should occur*/
        try {
            String messageId1 = "1234567890-123456789-01234567890/1234567890/`~!@#$%^&*()-_=+\\|,<.>/?;:'\"|\\[{]}.567890.1234567890-1234567890?1234567890#1234567890!1234567890$1234567890%1234567890|12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012";
            backendMessageValidatorObj.validateMessageId(messageId1);

            String messageId1_1 = "40b0-9ffc-3f4cfa88bf8b@domibus.eu";
            backendMessageValidatorObj.validateMessageId(messageId1_1);

            String messageId1_2 = "APP-RESPONSE-d8d85972-64fb-4161-a1fb-996aa7a9c39c-DOCUMENT-BUNDLE";
            backendMessageValidatorObj.validateMessageId(messageId1_2);

            String messageId1_3 = "<1234>";
            backendMessageValidatorObj.validateMessageId(messageId1_3);

            String messageId1_4 = "^12^3$4";
            backendMessageValidatorObj.validateMessageId(messageId1_4);

        } catch (EbMS3Exception e1) {
            Assert.fail("Exception was not expected in happy scenarios");
        }
         /*Happy Flow No error should occur*/

        /*Message Id with leading and/or trailing whitespaces should throw error*/
        try {
            String messageId2 = "\t\t346ea37f-7583-40b0-9ffc-3f4cfa88bf8b@domibus.eu\t\t";
            backendMessageValidatorObj.validateMessageId(messageId2);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id with leading and/or trailing whitespaces should throw error*/


        /*Message Id containing non printable control characters should result in error*/
        try {
            String messageId4 = "346ea\b37f-7583-40\u0010b0-9ffc-3f4\u007Fcfa88bf8b@d\u0001omibus.eu";
            backendMessageValidatorObj.validateMessageId(messageId4);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id containing non printable control characters should result in error*/

        /*Message Id containing only non printable control characters should result in error*/
        try {
            String messageId5 = "\b\u0010\u0030\u007F\u0001";
            backendMessageValidatorObj.validateMessageId(messageId5);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id containing non printable control characters should result in error*/

        /*Message id more than 255 characters long should result in error*/
        try {
            String messageId6 = "1234567890-123456789-01234567890/1234567890/1234567890.1234567890.123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890@domibus.eu";
            backendMessageValidatorObj.validateMessageId(messageId6);
            Assert.fail("Expected exception EBMS_0008 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0008", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message id more than 255 characters long should result in error*/

        /*Message id should not be null*/
        try {
            String messageId8 = null;
            backendMessageValidatorObj.validateMessageId(messageId8);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message id should not be null*/

    }


    @Test
    public void validateRefToMessageId() throws Exception {

        new Expectations() {{
            domibusProperties.getProperty(BackendMessageValidator.KEY_MESSAGEID_PATTERN);
            result = MESSAGE_ID_PATTERN;

        }};

        /*Happy Flow No error should occur*/
        try {
            String refTomessageId1 = "1234567890-123456789-01234567890/1234567890/`~!@#$%^&*()-_=+\\|,<.>/?;:'\"|\\[{]}.567890.1234567890-1234567890?1234567890#1234567890!1234567890$1234567890%1234567890|12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1);

            String refTomessageId1_1 = "40b0-9ffc-3f4cfa88bf8b@domibus.eu";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1_1);

            String refTomessageId1_2 = "APP-RESPONSE-d8d85972-64fb-4161-a1fb-996aa7a9c39c-DOCUMENT-BUNDLE";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1_2);

            String refTomessageId1_3 = "<1234>";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1_3);

            String refTomessageId1_4 = "^12^3$4";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1_4);

        } catch (EbMS3Exception e1) {
            Assert.fail("Exception was not expected in happy scenarios");
        }
         /*Happy Flow No error should occur*/

        /*Message Id with leading and/or trailing whitespaces should throw error*/
        try {
            String refTomessageId2 = "\t\t346ea37f-7583-40b0-9ffc-3f4cfa88bf8b@domibus.eu\t\t";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId2);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id with leading and/or trailing whitespaces should throw error*/


        /*Message Id containing non printable control characters should result in error*/
        try {
            String refTomessageId4 = "346ea\b37f-7583-40\u0010b0-9ffc-3f4\u007Fcfa88bf8b@d\u0001omibus.eu";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId4);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id containing non printable control characters should result in error*/

        /*Message Id containing only non printable control characters should result in error*/
        try {
            String refTomessageId5 = "\b\u0010\u0030\u007F\u0001";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId5);
            Assert.fail("Expected exception EBMS_0009 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0009", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message Id containing non printable control characters should result in error*/

        /*Message id more than 255 characters long should result in error*/
        try {
            String refTomessageId6 = "1234567890-123456789-01234567890/1234567890/1234567890.1234567890.123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890@domibus.eu";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId6);
            Assert.fail("Expected exception EBMS_0008 was not raised!");
        } catch (EbMS3Exception e2) {
            Assert.assertEquals("EBMS:0008", e2.getErrorCode().getCode().getErrorCode().getErrorCodeName());
        }
        /*Message id more than 255 characters long should result in error*/

        /*Ref To Message id can be null*/
        try {
            backendMessageValidatorObj.validateRefToMessageId(null);

        } catch (EbMS3Exception e2) {
            Assert.fail("RefToMessageId is an optional element and null should be handled!");
        }
        /*Ref To Message id can be null*/

    }

    @Test
    public void testConfigurationNotSpecified() {

        new Expectations() {{
            domibusProperties.getProperty(BackendMessageValidator.KEY_MESSAGEID_PATTERN);
            result = null;
        }};

        /*If the domibus-configuration file does not have the message id format, then message id pattern validation must be skipped. No exception expected*/
        try {
            String refTomessageId1 = "1234567890-123456789-01234567890/1234567890/`~!@#$%^&*()-_=+\\|,<.>/?;:'\"|\\[{]}.567890.1234567890-1234567890?1234567890#1234567890!1234567890$1234567890%1234567890|12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012";
            backendMessageValidatorObj.validateMessageId(refTomessageId1);

            String refTomessageId1_1 = "40b0-9ffc-3f4cfa88bf8b@domibus.eu";
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId1_1);

        } catch (Exception e1) {
            Assert.fail("When MessageId pattern configuration is not specified, then skip the format validation and no exception is expected!!");
        }
    }

    @Test
    /* Verifies that the initiator and the responder parties are different. */
    public void validatePartiesOk() throws Exception {

        final Party from = new Party();
        from.setName(RED);

        final Party to = new Party();
        to.setName(BLUE);

        backendMessageValidatorObj.validateParties(from, to);

        new Verifications() {{
            Assert.assertNotEquals(from, to);
        }};

    }

    @Test
    /* Verifies that the initiator and the responder parties are the same. */
    public void validatePartiesNOk() throws Exception {

        final Party from = new Party();
        from.setName(BLUE);

        final Party to = new Party();
        to.setName(BLUE);

        try {
            backendMessageValidatorObj.validateParties(from, to);
            Assert.fail("It should throw " + EbMS3Exception.class.getCanonicalName());
        } catch (EbMS3Exception ex) {
            assert (ex.getErrorCode().equals(ErrorCode.EbMS3ErrorCode.EBMS_0010));
            assert (ex.getErrorDetail().contains("The initiator party's name is the same as the responder party's one"));
        }

    }

    @Test
    /* Verifies that the message is being sent by the same party as the one configured for the sending access point */
    public void validateInitiatorPartyOk() throws Exception {

        final Party gatewayParty = new Party();
        gatewayParty.setName(BLUE);

        final Party from = new Party();
        from.setName(BLUE);

        backendMessageValidatorObj.validateInitiatorParty(gatewayParty, from);

        new Verifications() {{
            Assert.assertEquals(gatewayParty, from);
        }};

    }

    @Test
    /* Verifies that the message is NOT being sent by the same party as the one configured for the sending access point */
    public void validateInitiatorPartyNOk() throws Exception {

        final Party gatewayParty = new Party();
        gatewayParty.setName(RED);

        final Party from = new Party();
        from.setName(BLUE);

        try {
            backendMessageValidatorObj.validateInitiatorParty(gatewayParty, from);
            Assert.fail("It should throw " + EbMS3Exception.class.getCanonicalName());
        } catch (EbMS3Exception ex) {
            assert (ex.getErrorCode().equals(ErrorCode.EbMS3ErrorCode.EBMS_0010));
            assert (ex.getErrorDetail().contains("does not correspond to the access point's name"));
        }

    }

    @Test
    /* Verifies that the message is not for the current gateway. */
    public void validateResponderPartyOk() throws Exception {

        final Party gatewayParty = new Party();
        gatewayParty.setName(BLUE);

        final Party to = new Party();
        to.setName(RED);

        backendMessageValidatorObj.validateResponderParty(gatewayParty, to);

        new Verifications() {{
            Assert.assertNotEquals(gatewayParty, to);
        }};

    }

    @Test
    /* Verifies that the message is wrongly sent to current gateway.*/
    public void validateResponderPartyNOk() throws Exception {

        final Party gatewayParty = new Party();
        gatewayParty.setName(BLUE);

        final Party to = new Party();
        to.setName(BLUE);

        try {
            backendMessageValidatorObj.validateResponderParty(gatewayParty, to);
            Assert.fail("It should throw " + EbMS3Exception.class.getCanonicalName());
        } catch (EbMS3Exception ex) {
            assert (ex.getErrorCode().equals(ErrorCode.EbMS3ErrorCode.EBMS_0010));
            assert (ex.getErrorDetail().contains("It is forbidden to submit a message to the sending access point"));
        }

    }


    @Test
    /* Verifies that the parties' roles are different. */
    public void validatePartiesRolesOk() throws Exception {

        final Role fromRole = new Role();
        fromRole.setName(INITIATOR_ROLE);

        final Role toRole = new Role();
        toRole.setName(RESPONDER_ROLE);

        backendMessageValidatorObj.validatePartiesRoles(fromRole, toRole);

        new Verifications() {{
            Assert.assertNotEquals(fromRole, toRole);
        }};

    }

    @Test
    /* Verifies that the parties' roles are the same. */
    public void validatePartiesRolesNOk() throws Exception {

        final Role fromRole = new Role();
        fromRole.setName(INITIATOR_ROLE);

        final Role toRole = new Role();
        toRole.setName(INITIATOR_ROLE);

        try {
            backendMessageValidatorObj.validatePartiesRoles(fromRole, toRole);
            Assert.fail("It should throw " + EbMS3Exception.class.getCanonicalName());
        } catch (EbMS3Exception ex) {
            assert (ex.getErrorCode().equals(ErrorCode.EbMS3ErrorCode.EBMS_0010));
            assert (ex.getErrorDetail().contains("The initiator party's role is the same as the responder party's one"));
        }

    }


    //TODO use this when the domibus-configuration.xml is moved in the domibus-test module classpath
    /*protected String loadMessageIdPatternFromConfigurationFile() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
        MessageIdPatternRetriever messageIdPatternRetriever = new MessageIdPatternRetriever();
        if (messageIdPatternRetriever.getMessageIdPattern() == null) {
            File f = new File(getClass().getClassLoader().getResource(DOMIBUS_CONFIGURATION_FILE).toURI());
            InputSource is = new InputSource(new FileInputStream(f));
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(messageIdPatternRetriever);
            xmlReader.parse(is);
        }
        return messageIdPatternRetriever.getMessageIdPattern();
    }*/
}

class MessageIdPatternRetriever extends DefaultHandler {

    private static final String PROP_KEY = "prop";
    private boolean bHasMessageIdPattern;
    private static String MessageIdPattern;

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (PROP_KEY.equalsIgnoreCase(qName) && BackendMessageValidator.KEY_MESSAGEID_PATTERN.equalsIgnoreCase(atts.getValue("key"))) {
            bHasMessageIdPattern = true;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (bHasMessageIdPattern) {
            MessageIdPattern = new String(ch, start, length);
            bHasMessageIdPattern = false;
        }
    }

    public String getMessageIdPattern() {
        return MessageIdPattern;
    }
}
