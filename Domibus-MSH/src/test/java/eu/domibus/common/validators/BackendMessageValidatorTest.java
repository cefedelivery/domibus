package eu.domibus.common.validators;

import eu.domibus.common.exception.EbMS3Exception;
import mockit.Injectable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by venugar on 11/10/2016.
 */


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring-context.xml")
public class BackendMessageValidatorTest {

    private static final Log LOG = LogFactory.getLog(BackendMessageValidatorTest.class);
    private static boolean initialized;

    @Autowired
    BackendMessageValidator backendMessageValidatorObj;

    @Injectable
    Properties domibusProperties;

    @BeforeClass
    public static void init() throws IOException {
        if (!initialized) {
            System.setProperty("domibus.config.location", new File("target/test-classes").getAbsolutePath());
            initialized = true;
        }

    }

    @Test
    public void validateMessageId() throws Exception {

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

        /*Message id should not be null*/
        try {
            String refTomessageId8 = null;
            backendMessageValidatorObj.validateRefToMessageId(refTomessageId8);

        } catch (EbMS3Exception e2) {
            Assert.fail("RefToMessageId is an optional element and null should be handled!");
        }
        /*Message id should not be null*/

    }

}