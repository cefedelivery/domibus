package eu.domibus.plugin.jms;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.ErrorResultImpl;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.NotificationType;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;

import javax.jms.MapMessage;

import static org.junit.Assert.assertEquals;


/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class BackendJMSImplTest {

    @Injectable
    protected MessageRetriever<Submission> messageRetriever;

    @Injectable
    protected MessageSubmitter<Submission> messageSubmitter;

    @Injectable
    private JmsOperations replyJmsTemplate;

    @Injectable
    private JmsOperations mshToBackendTemplate;

    @Injectable
    private JmsOperations errorNotifyConsumerTemplate;

    @Injectable
    private JmsOperations errorNotifyProducerTemplate;

    @Injectable
    String name = "myjmsplugin";

    @Tested
    BackendJMSImpl backendJMS;

    @Test
    public void testReceiveMessage(@Injectable final MapMessage map) throws Exception {
        final String messageId = "1";
        final String jmsCorrelationId = "2";
        final String messageTypeSubmit = JMSMessageConstants.MESSAGE_TYPE_SUBMIT;

        new Expectations(backendJMS) {{
            map.getStringProperty(JMSMessageConstants.MESSAGE_ID);
            result = messageId;

            map.getJMSCorrelationID();
            result = jmsCorrelationId;

            map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY);
            result = messageTypeSubmit;

            backendJMS.submit(withAny(new ActiveMQMapMessage()));
            result = messageId;
        }};

        backendJMS.receiveMessage(map);

        new Verifications() {{
            backendJMS.submit(map);

            String capturedMessageId = null;
            String capturedJmsCorrelationId = null;
            String capturedErrorMessage = null;
            backendJMS.sendReplyMessage(capturedMessageId = withCapture(), capturedJmsCorrelationId = withCapture(), capturedJmsCorrelationId = withCapture());

            assertEquals(capturedMessageId, messageId);
            assertEquals(capturedJmsCorrelationId, jmsCorrelationId);
        }};
    }

    @Test
    public void testReceiveMessageWithUnacceptedMessage(@Injectable final MapMessage map) throws Exception {
        final String messageId = "1";
        final String jmsCorrelationId = "2";
        final String unacceptedMessageType = "unacceptedMessageType";

        new Expectations(backendJMS) {{
            map.getStringProperty(JMSMessageConstants.MESSAGE_ID);
            result = messageId;

            map.getJMSCorrelationID();
            result = jmsCorrelationId;

            map.getStringProperty(JMSMessageConstants.JMS_BACKEND_MESSAGE_TYPE_PROPERTY_KEY);
            result = unacceptedMessageType;
        }};

        backendJMS.receiveMessage(map);

        new Verifications() {{
            String capturedMessageId = null;
            String capturedJmsCorrelationId = null;
            String capturedErrorMessage = null;
            backendJMS.sendReplyMessage(capturedMessageId = withCapture(), capturedJmsCorrelationId = withCapture(), capturedJmsCorrelationId = withCapture());

            assertEquals(capturedMessageId, messageId);
            assertEquals(capturedJmsCorrelationId, jmsCorrelationId);
        }};
    }

    @Test
    public void testMessageReceiveFailed(@Mocked ErrorMessageCreator errorMessageCreator) throws Exception {
        MessageReceiveFailureEvent event = new MessageReceiveFailureEvent();
        final ErrorResult errorResult = new ErrorResultImpl();
        event.setErrorResult(errorResult);
        final String myEndpoint = "myEndpoint";
        event.setEndpoint(myEndpoint);
        final String messageId = "1";
        event.setMessageId(messageId);

        backendJMS.messageReceiveFailed(event);

        new Verifications() {{
            withCapture(new ErrorMessageCreator(errorResult,
                    myEndpoint,
                    NotificationType.MESSAGE_RECEIVED_FAILURE));

        }};
    }
}
