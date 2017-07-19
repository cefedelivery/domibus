package eu.domibus.plugin.fs;

import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.AbstractBackendConnector;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

import javax.annotation.PostConstruct;

/**
 * File system backend integration plugin.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class BackendFSImpl extends AbstractBackendConnector<FSMessage, FSMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendFSImpl.class);

    /**
     * Creates a new <code>BackendFSImpl</code>.
     *
     * @param name Connector name
     */
    public BackendFSImpl(String name) {
        super(name);
    }

    @PostConstruct
    public void init() {
        LOG.info("The File System Plugin is initialized.");
    }

    /**
     * The implementations of the transformer classes are responsible for
     * transformation between the native backend formats and
     * eu.domibus.plugin.Submission.
     *
     * @return MessageSubmissionTransformer
     */
    @Override
    public MessageSubmissionTransformer<FSMessage> getMessageSubmissionTransformer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * The implementations of the transformer classes are responsible for
     * transformation between the native backend formats and
     * eu.domibus.plugin.Submission.
     *
     * @return MessageRetrievalTransformer
     */
    @Override
    public MessageRetrievalTransformer<FSMessage> getMessageRetrievalTransformer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deliverMessage(String messageId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void messageSendFailed(String messageId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void messageSendSuccess(String messageId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
