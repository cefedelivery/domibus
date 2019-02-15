package eu.domibus.plugin.webService.impl;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.ext.services.MessageAcknowledgeExtService;
import eu.domibus.ext.services.MessageExtService;
import eu.domibus.plugin.MessageLister;
import eu.domibus.plugin.handler.MessagePuller;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.plugin.webService.generated.LargePayloadType;
import eu.domibus.plugin.webService.generated.RetrieveMessageFault;
import eu.domibus.plugin.webService.generated.RetrieveMessageRequest;
import eu.domibus.plugin.webService.generated.RetrieveMessageResponse;
import eu.domibus.plugin.webService.generated.StatusFault;
import eu.domibus.plugin.webService.generated.SubmitMessageFault;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import eu.domibus.plugin.webService.generated.StatusRequest;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 4.0.2
 */
@RunWith(JMockit.class)
public class BackendWebServiceImplTest {

    @Tested
    private BackendWebServiceImpl backendWebService;

    @Injectable
    private StubDtoTransformer defaultTransformer;

    @Injectable
    private MessageAcknowledgeExtService messageAcknowledgeExtService;

    @Injectable
    private  MessageRetriever messageRetriever;

    @Injectable
    private  MessageSubmitter messageSubmitter;

    @Injectable
    private MessagePuller messagePuller;

    @Injectable
    private MessageExtService messageExtService;

    @Injectable
    private MessageLister lister;

    @Injectable
    private String name;

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

    @Test
    public void cleansTheMessageIdentifierBeforeRetrievingTheMessageByItsIdentifier(@Injectable RetrieveMessageRequest retrieveMessageRequest,
                                                                                    @Injectable RetrieveMessageResponse retrieveMessageResponse,
                                                                                    @Injectable Messaging ebMSHeaderInfo) throws RetrieveMessageFault {
        new Expectations() {{
            retrieveMessageRequest.getMessageID();
            result = "-Dom137--";

        }};

        backendWebService.retrieveMessage(retrieveMessageRequest, new Holder<RetrieveMessageResponse>(retrieveMessageResponse), new Holder<>(ebMSHeaderInfo));

        new Verifications() {{
            String messageId;
            messageExtService.cleanMessageIdentifier(messageId = withCapture());
            assertEquals("The message identifier should have been cleaned before retrieving the message", "-Dom137--", messageId);
        }};
    }

    @Test
    public void cleansTheMessageIdentifierBeforeRetrievingTheStatusOfAMessageByItsIdentifier(@Injectable StatusRequest statusRequest) throws StatusFault {
        new Expectations() {{
            statusRequest.getMessageID();
            result = "-Dom138--";

        }};

        backendWebService.getStatus(statusRequest);

        new Verifications() {{
            String messageId;
            messageExtService.cleanMessageIdentifier(messageId = withCapture());
            assertEquals("The message identifier should have been cleaned before retrieving the message", "-Dom138--", messageId);
        }};
    }
}