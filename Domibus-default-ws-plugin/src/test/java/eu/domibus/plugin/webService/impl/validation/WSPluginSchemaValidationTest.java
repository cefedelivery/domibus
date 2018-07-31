package eu.domibus.plugin.webService.impl.validation;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.plugin.webService.generated.*;
import eu.domibus.plugin.webService.impl.BackendWebServiceFaultFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapFault;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.activation.DataHandler;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@RunWith(JMockit.class)
public class WSPluginSchemaValidationTest {

    @Tested
    WSPluginSchemaValidation wsPluginSchemaValidation;

    @Injectable
    protected BackendWebServiceFaultFactory backendWebServiceFaultFactory;

    @Injectable
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Test
    public void testSchemaValidationEnabled() {
        new Expectations() {{
            domibusPropertyExtService.getProperty("wsplugin.schema.validation.enabled", "true");
            result = "true";

        }};

        assertTrue(wsPluginSchemaValidation.schemaValidationEnabled());
    }

    @Test
    public void testValidateSubmitMessage(@Injectable SubmitRequest submitRequest, @Injectable Messaging ebMSHeaderInfo, @Injectable XMLValidationErrorHandler messagingValidationResult) throws Exception {
        new Expectations(wsPluginSchemaValidation) {{
            domibusPropertyExtService.getProperty("wsplugin.schema.validation.enabled", "true");
            result = "true";

            wsPluginSchemaValidation.validateMessaging(ebMSHeaderInfo);
            result = messagingValidationResult;

            messagingValidationResult.isValid();
            result = false;

        }};

        try {
            wsPluginSchemaValidation.validateSubmitMessage(submitRequest, ebMSHeaderInfo);
            fail("The request is not valid and it should have thrown exception");
        } catch (SubmitMessageFault e) {

        }

        new Verifications() {{
            wsPluginSchemaValidation.validateSubmitRequest(submitRequest);
            times = 0;
        }};
    }

    @Test
    public void testValidateRetrieveMessageRequest(@Injectable RetrieveMessageRequest retrieveMessageRequest, @Injectable XMLValidationErrorHandler messagingValidationResult) throws Exception {
        new Expectations(wsPluginSchemaValidation) {{
            domibusPropertyExtService.getProperty("wsplugin.schema.validation.enabled", "true");
            result = "true";

            wsPluginSchemaValidation.validateRetrieveMessageRequest(retrieveMessageRequest);
            result = messagingValidationResult;

            messagingValidationResult.isValid();
            result = true;
        }};

        try {
            wsPluginSchemaValidation.validateRetrieveMessage(retrieveMessageRequest);
        } catch (RetrieveMessageFault e) {
            fail("The request is valid and no fault should be thrown");
        }
    }


    @Test
    public void testValidateGetStatus(@Injectable StatusRequest statusRequest, @Injectable XMLValidationErrorHandler messagingValidationResult) throws Exception {
        new Expectations(wsPluginSchemaValidation) {{
            domibusPropertyExtService.getProperty("wsplugin.schema.validation.enabled", "true");
            result = "true";

            wsPluginSchemaValidation.validateGetStatusRequest(statusRequest);
            result = messagingValidationResult;

            messagingValidationResult.isValid();
            result = true;
        }};

        try {
            wsPluginSchemaValidation.validateGetStatus(statusRequest);
        } catch (StatusFault e) {
            fail("The request is valid and no fault should be thrown");
        }
    }

    @Test
    public void testValidateGetMessageErrorsRequest(@Injectable GetErrorsRequest messageErrorsRequest, @Injectable XMLValidationErrorHandler messagingValidationResult) throws Exception {
        new Expectations(wsPluginSchemaValidation) {{
            domibusPropertyExtService.getProperty("wsplugin.schema.validation.enabled", "true");
            result = "true";

            wsPluginSchemaValidation.validateGetMessageErrors(messageErrorsRequest);
            result = messagingValidationResult;

            messagingValidationResult.isValid();
            result = true;
        }};

        try {
            wsPluginSchemaValidation.validateGetMessageErrorsRequest(messageErrorsRequest);
        } catch (SoapFault e) {
            fail("The request is valid and no fault should be thrown");
        }
    }

    @Test
    public void testValidateSubmitRequest(@Injectable SubmitRequest submitRequest,
                                          @Injectable LargePayloadType payloadType1, @Injectable DataHandler dataHandler1,
                                          @Injectable LargePayloadType payloadType2, @Injectable DataHandler dataHandler2) throws Exception {
        List<LargePayloadType> payloads = new ArrayList<>();
        payloads.add(payloadType1);
        payloads.add(payloadType2);

        new Expectations() {{
            submitRequest.getPayload();
            result = payloads;

            payloadType1.getValue();
            result = dataHandler1;

            payloadType2.getValue();
            result = dataHandler2;
        }};

        wsPluginSchemaValidation.validateSubmitRequest(submitRequest);

        new Verifications() {{
            payloadType1.getValue();
            times = 1;

            payloadType2.getValue();
            times = 1;
        }};

        assertTrue(payloadType1.getValue() == dataHandler1);
        assertTrue(payloadType2.getValue() == dataHandler2);
    }
}
