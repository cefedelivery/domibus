package eu.domibus.plugin.delegate;

import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public class MessageReceiveDeprecatedPluginImplementationTest extends AbstractBackendConnector {

    public MessageReceiveDeprecatedPluginImplementationTest(String name) {
        super(name);
    }

    @Override
    public void messageReceiveFailed(String messageId, String ednpoint) {
        super.messageReceiveFailed(messageId, ednpoint);
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
