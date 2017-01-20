package eu.domibus.plugin.delegate;

import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public class MessageReceivePluginImplementationTest extends AbstractBackendConnector {

    public MessageReceivePluginImplementationTest(String name) {
        super(name);
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        super.messageReceiveFailed(messageReceiveFailureEvent);
    }

    @Override
    public MessageSubmissionTransformer getMessageSubmissionTransformer() {
        return null;
    }

    @Override
    public MessageRetrievalTransformer getMessageRetrievalTransformer() {
        return null;
    }

    @Override
    public void messageSendFailed(String messageId) {

    }
}
