package eu.domibus.plugin.webService;

import eu.domibus.AbstractSendMessageIT;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Property;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.SubmitMessageFault;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import eu.domibus.plugin.webService.generated.SubmitResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This JUNIT implements the Test cases Validate Message Properties - 01, Validate Message Properties - 02, Validate Message Properties - 03,
 * Validate Message Properties - 04, Validate Message Properties - 05, Validate Message Properties - 06
 *
 * @author martifp
 */
@Ignore
public class SubmitMessageValidatePropertiesIT extends AbstractSendMessageIT {

    private static boolean initialized;
    @Autowired
    BackendInterface backendWebService;

    @Before
    public void before() throws IOException {
        if (!initialized) {
            insertDataset("sendMessageValidateProperties.sql");
            initialized = true;
        }
    }

    /**
     * Tests that a send message fails because one or more mandatory properties are missing in the user message.
     * Validate Message Properties - 01
     *
     * @throws SubmitMessageFault
     */
    @Test(expected = SubmitMessageFault.class)
    public void testSendMessagePropertiesRequiredMissing() throws SubmitMessageFault {

        String payloadHref = "payload";
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref, null);
        System.out.println("Props: " + ebMSHeaderInfo.getUserMessage().getMessageProperties().getProperty());

        try {
            backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);
        } catch (SubmitMessageFault re) {
            String message = "Message submission failed";
            Assert.assertEquals(message, re.getMessage());
            throw re;
        }
        Assert.fail("SubmitMessageFault was expected but was not raised");
    }

    /**
     * Tests that a send message fails because the int property value is wrong.
     * Validate Message Properties - 02
     *
     * @throws SubmitMessageFault
     */
    @Test(expected = SubmitMessageFault.class)
    public void testSubmitMessageWrongIntValue() throws SubmitMessageFault {

        String payloadHref = "payload";
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref, null);
        List<Property> properties = ebMSHeaderInfo.getUserMessage().getMessageProperties().getProperty();
        properties.add(super.createProperty("aNumber", "xxx", INT_TYPE));
        properties.add(super.createProperty("aBoolean", "true", BOOLEAN_TYPE));
        System.out.println("Props: " + ebMSHeaderInfo.getUserMessage().getMessageProperties().getProperty());

        try {
            backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);
        } catch (SubmitMessageFault re) {
            String message = "Message submission failed";
            Assert.assertEquals(message, re.getMessage());
            throw re;
        }
        Assert.fail("SubmitMessageFault was expected but was not raised");
    }

    /**
     * Test that a send message fails because the boolean property value is wrong.
     * Validate Message Properties - 03
     *
     * @throws SubmitMessageFault
     */
    @Test(expected = SubmitMessageFault.class)
    public void testSubmitMessageWrongBooleanValue() throws SubmitMessageFault {

        String payloadHref = "payload";
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref, null);
        List<Property> properties = ebMSHeaderInfo.getUserMessage().getMessageProperties().getProperty();
        properties.add(super.createProperty("aNumber", "123", INT_TYPE));
        properties.add(super.createProperty("aBoolean", "ciao", BOOLEAN_TYPE));
        System.out.println("Props: " + ebMSHeaderInfo.getUserMessage().getMessageProperties().getProperty());

        try {
            backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);
        } catch (SubmitMessageFault re) {
            String message = "Message submission failed";
            Assert.assertEquals(message, re.getMessage());
            throw re;
        }
        Assert.fail("SubmitMessageFault was expected but was not raised");
    }

    /**
     * Test that a send message fails because the String property value is null.
     * Validate Message Properties - 04
     *
     * @throws SubmitMessageFault
     */
    //@Test(expected = SubmitMessageFault.class)
    public void testSubmitMessageWrongStringValue() throws SubmitMessageFault {

        String payloadHref = "payload";
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref, null);
        List<Property> properties = ebMSHeaderInfo.getUserMessage().getMessageProperties().getProperty();
        Property aStringProperty = properties.iterator().next();
        aStringProperty.setValue(null);
        properties.add(super.createProperty("aNumber", "123", INT_TYPE));
        properties.add(super.createProperty("aBoolean", "true", BOOLEAN_TYPE));
        System.out.println("Props: " + ebMSHeaderInfo.getUserMessage().getMessageProperties().getProperty());

        try {
            backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);
        } catch (SubmitMessageFault re) {
            String message = "Message submission failed";
            Assert.assertEquals(message, re.getMessage());
            throw re;
        }
        Assert.fail("SubmitMessageFault was expected but was not raised");
    }

    /**
     * Test that a send message is successful with all the 4 properties covering all possible types.
     * Validate Message Properties - 05
     *
     * @throws SubmitMessageFault
     */
    @Test
    public void testSubmitMessagePropertiesRequiredOk() throws SubmitMessageFault, SQLException, InterruptedException {

        String payloadHref = "payload";
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref, null);

        List<Property> properties = ebMSHeaderInfo.getUserMessage().getMessageProperties().getProperty();
        properties.add(super.createProperty("aNumber", "123", INT_TYPE));
        properties.add(super.createProperty("aBoolean", "true", BOOLEAN_TYPE));
        System.out.println("Props: " + ebMSHeaderInfo.getUserMessage().getMessageProperties().getProperty());

        super.prepareSendMessage("validAS4Response.xml");
        SubmitResponse response = backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);
        verifySendMessageAck(response);
    }

    /**
     * Test that a send message fails because the property type is wrong.
     * Validate Message Properties - 06
     *
     * @throws SubmitMessageFault
     */
    @Test(expected = SubmitMessageFault.class)
    public void testSubmitMessageWrongType() throws SubmitMessageFault {

        String payloadHref = "payload";
        SubmitRequest submitRequest = createSubmitRequest(payloadHref);
        Messaging ebMSHeaderInfo = createMessageHeader(payloadHref, null);
        List<Property> properties = ebMSHeaderInfo.getUserMessage().getMessageProperties().getProperty();
        properties.add(super.createProperty("aNumber", "123", INT_TYPE));
        properties.add(super.createProperty("aBoolean", "ciao", BOOLEAN_TYPE));
        properties.add(super.createProperty("aFloat", "23.45", "float"));
        System.out.println("Props: " + ebMSHeaderInfo.getUserMessage().getMessageProperties().getProperty());

        try {
            backendWebService.submitMessage(submitRequest, ebMSHeaderInfo);
        } catch (SubmitMessageFault re) {
            String message = "Message submission failed";
            Assert.assertEquals(message, re.getMessage());
            throw re;
        }
        Assert.fail("SubmitMessageFault was expected but was not raised");
    }
}