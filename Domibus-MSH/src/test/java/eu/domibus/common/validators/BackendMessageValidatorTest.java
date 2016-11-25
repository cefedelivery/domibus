package eu.domibus.common.validators;

import eu.domibus.common.exception.EbMS3Exception;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log LOG = LogFactory.getLog(BackendMessageValidatorTest.class);

    private static final String DOMIBUS_CONFIGURATION_FILE = "domibus-configuration.xml";

    @Injectable
    Properties domibusProperties;

    @Tested
    BackendMessageValidator backendMessageValidatorObj;


    @Test
    public void validateMessageId() throws Exception {

        new Expectations() {{
            domibusProperties.getProperty(BackendMessageValidator.KEY_MESSAGEID_PATTERN);
            result = loadMessageIdPatternFromConfigurationFile();

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
            LOG.error(e1);
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
            result = loadMessageIdPatternFromConfigurationFile();

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
            LOG.error(e1);
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
            LOG.error(e1);
            Assert.fail("When MessageId pattern configuration is not specified, then skip the format validation and no exception is expected!!");
        }
    }


    protected String loadMessageIdPatternFromConfigurationFile() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
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
    }
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