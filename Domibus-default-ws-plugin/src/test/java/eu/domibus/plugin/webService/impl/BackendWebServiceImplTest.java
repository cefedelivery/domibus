package eu.domibus.plugin.webService.impl;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.ext.services.MessageAcknowledgeExtService;
import eu.domibus.plugin.handler.MessagePuller;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.plugin.webService.generated.LargePayloadType;
import eu.domibus.plugin.webService.generated.SubmitMessageFault;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0.2
 */
@RunWith(JMockit.class)
public class BackendWebServiceImplTest {

    @Tested
    BackendWebServiceImpl backendWebService;

    @Injectable
    private StubDtoTransformer defaultTransformer;

    @Injectable
    private MessageAcknowledgeExtService messageAcknowledgeExtService;

    @Injectable
    protected MessageRetriever messageRetriever;

    @Injectable
    protected MessageSubmitter messageSubmitter;

    @Injectable
    MessagePuller messagePuller;

    @Injectable
    String name;

    @Test(expected = SubmitMessageFault.class)
    public void validateSubmitRequestWithPayloadsAndBodyload(@Injectable SubmitRequest submitRequest,
                                                             @Injectable Messaging ebMSHeaderInfo,
                                                             @Injectable LargePayloadType payload1,
                                                             @Injectable LargePayloadType payload2,
                                                             @Injectable LargePayloadType bodyload) throws SubmitMessageFault {
        List<LargePayloadType> payloadList = new ArrayList<>();
        payloadList.add(payload1);
        payloadList.add(payload2);

        new Expectations() {{
            submitRequest.getPayload();
            result = payloadList;

            payload1.getPayloadId();
            result = "cid:message1";

            payload2.getPayloadId();
            result = "cid:message2";

            submitRequest.getBodyload();
            result = bodyload;

            bodyload.getPayloadId();
            result = "null";
        }};

        backendWebService.validateSubmitRequest(submitRequest, ebMSHeaderInfo);
    }

    @Test(expected = SubmitMessageFault.class)
    public void validateSubmitRequestWithMissingPayloadIdForPayload(@Injectable SubmitRequest submitRequest,
                                                                    @Injectable Messaging ebMSHeaderInfo,
                                                                    @Injectable LargePayloadType payload1) throws SubmitMessageFault {
        List<LargePayloadType> payloadList = new ArrayList<>();
        payloadList.add(payload1);

        new Expectations() {{
            submitRequest.getPayload();
            result = payloadList;

            payload1.getPayloadId();
            result = null;
        }};

        backendWebService.validateSubmitRequest(submitRequest, ebMSHeaderInfo);
    }

    @Test(expected = SubmitMessageFault.class)
    public void validateSubmitRequestWithPayloadIdAddedForBodyload(@Injectable SubmitRequest submitRequest,
                                                                   @Injectable Messaging ebMSHeaderInfo,
                                                                   @Injectable LargePayloadType bodyload) throws SubmitMessageFault {

        new Expectations() {{
            submitRequest.getBodyload();
            result = bodyload;

            bodyload.getPayloadId();
            result = "cid:message";
        }};

        backendWebService.validateSubmitRequest(submitRequest, ebMSHeaderInfo);
    }
}