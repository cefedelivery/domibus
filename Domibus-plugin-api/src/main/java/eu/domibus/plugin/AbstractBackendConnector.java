package eu.domibus.plugin;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.MessageStatus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.messaging.PModeMismatchException;
import eu.domibus.plugin.exception.TransformationException;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.handler.MessageSubmitter;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Base class for implementing plugins
 *
 * @author Christian Koch, Stefan Mueller
 */
public abstract class AbstractBackendConnector<U, T> implements BackendConnector<U, T> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractBackendConnector.class);

    private final String name;
    @Autowired
    protected MessageRetriever<Submission> messageRetriever;
    @Autowired
    protected MessageSubmitter<Submission> messageSubmitter;
    private MessageLister lister;

    public AbstractBackendConnector(final String name) {
        this.name = name;
    }

    public void setLister(final MessageLister lister) {
        this.lister = lister;
    }

    public abstract MessageSubmissionTransformer<U> getMessageSubmissionTransformer();

    public abstract MessageRetrievalTransformer<T> getMessageRetrievalTransformer();

    @Override
    // The following does not have effect at this level since the transaction would have already been rolled back!
    // @Transactional(noRollbackFor = {IllegalArgumentException.class, IllegalStateException.class})
    public String submit(final U message) throws MessagingProcessingException {
        try {
            final Submission messageData = getMessageSubmissionTransformer().transformToSubmission(message);
            return this.messageSubmitter.submit(messageData, this.getName());
        } catch (IllegalArgumentException iaEx) {
            throw new TransformationException(iaEx);
        } catch (IllegalStateException ise) {
            throw new PModeMismatchException(ise);
        }
    }



    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public T downloadMessage(final String messageId, final T target) throws MessageNotFoundException {
        lister.removeFromPending(messageId);
        return this.getMessageRetrievalTransformer().transformFromSubmission(this.messageRetriever.downloadMessage(messageId), target);
    }


    @Override
    public Collection<String> listPendingMessages() {
        return lister.listPendingMessages();
    }


    @Override
    public MessageStatus getMessageStatus(final String messageId) {
        return this.messageRetriever.getMessageStatus(messageId);
    }

    @Override
    public MessageStatus getStatus(final String messageId) {
        return this.messageRetriever.getStatus(messageId);
    }

    @Override
    public List<ErrorResult> getErrorsForMessage(final String messageId) {
        return new ArrayList<>(this.messageRetriever.getErrorsForMessage(messageId));
    }

    /**
     * @deprecated Since 3.2.2 this method is deprecated. Use {@link #messageReceiveFailed(MessageReceiveFailureEvent)}
     * @param messageId the Id of the failed message
     * @param ednpoint  the endpoint that tried to send the message or null if unknown
     */
    @Override
    @Deprecated
    public void messageReceiveFailed(String messageId, String ednpoint) {
        throw new UnsupportedOperationException("Method [messageReceiveFailed(String messageId, String endpoint)] is deprecated");
    }

    @Override
    public void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent) {
        throw new UnsupportedOperationException("Plugins using " + Mode.PUSH.name() + " must implement this method");
    }

    @Override
    public void deliverMessage(final String messageId) {
        throw new UnsupportedOperationException("Plugins using " + Mode.PUSH.name() + " must implement this method");
    }

    @Override
    public void messageSendSuccess(String messageId) {
        throw new UnsupportedOperationException("Plugins using " + Mode.PUSH.name() + " must implement this method");
    }

    @Override
    public String getName() {
        return name;
    }
}
