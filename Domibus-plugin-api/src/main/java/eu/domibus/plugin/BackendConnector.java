
package eu.domibus.plugin;

import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.MessageStatusChangeEvent;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

import java.util.Collection;
import java.util.List;

/**
 * Definition of a backend integration plugin. Direct implementation of this interface is NOT RECOMMENDED.
 * Implementations should extend eu.domibus.plugin.AbstractBackendConnector instead.
 *
 * @author Christian Koch, Stefan Mueller
 */
public interface BackendConnector<U, T> {


    /**
     * @return the MessageSubmissionTransformer matching the intended backend submission DTO
     */
    MessageSubmissionTransformer<U> getMessageSubmissionTransformer();

    /**
     * @return MessageRetrievalTransformer matching the intended backend retrieval DTO
     */
    MessageRetrievalTransformer<T> getMessageRetrievalTransformer();


    /**
     * Submits a message in the native format to the Domibus MSH
     *
     * @param message the message to be sent
     * @return the Domibus-internal Id of the message. This Id is used for all further reference to the submitted message.
     * @throws MessagingProcessingException If the message was rejected by the Domibus MSH
     */
    String submit(final U message) throws MessagingProcessingException;

    /**
     * provides the message with the corresponding messageId. A target object (i.e. an instance of javax.jms.Message)
     * can be provided. This is necessary in case the DTO for transfer to the backend is constructed by a
     * factory (i.e. a JMS session).
     *
     * @param messageId the messageId of the message to retrieve
     * @param target    the target object to be filled.
     * @return the message object with the given messageId
     */
    T downloadMessage(final String messageId, final T target) throws MessageNotFoundException;

    /**
     * provides a list of messageIds which have not been downloaded yet. Only available for Mode.PULL plugins
     *
     * @return a list of messages that have not been downloaded yet.
     * @throws UnsupportedOperationException when called from a plugin using Mode.PUSH
     */
    Collection<String> listPendingMessages();

    /**
     * Returns message status {@link eu.domibus.common.MessageStatus} for message with messageid
     *
     * @param messageId id of the message the status is requested for
     * @return the message status {@link eu.domibus.common.MessageStatus}
     * @deprecated since 3.3-rc1; this method converts DOWNLOADED status to RECEIVED to maintain
     * the backwards compatibility. Use {@link eu.domibus.plugin.BackendConnector#getStatus(String)} instead
     */
    @Deprecated
    MessageStatus getMessageStatus(final String messageId);

    /**
     * Returns message status {@link eu.domibus.common.MessageStatus} for message with messageid
     *
     * @param messageId id of the message the status is requested for
     * @return the message status {@link eu.domibus.common.MessageStatus}
     */
    MessageStatus getStatus(final String messageId);

    /**
     * Returns List {@link java.util.List} of error logs {@link ErrorResult} for message with messageid
     *
     * @param messageId id of the message the errors are requested for
     * @return the list of error log entries {@link java.util.List< ErrorResult >}
     */
    List<ErrorResult> getErrorsForMessage(final String messageId);

    /**
     * Delivers the message with the associated messageId to the backend application. Plugins working in Mode.PUSH mode
     * MUST OVERRIDE this method. For plugins working in Mode.PULL this method never gets called. The message can be
     * retrieved by a subclass by calling super.downloadMessage(messageId, target)
     *
     * @param messageId
     */
    void deliverMessage(final String messageId);

    /**
     * The name of the plugin instance. To allow for multiple deployments of the same plugin this value should be externalized.
     *
     * @return the name of the plugin
     */
    String getName();

    /**
     * @deprecated Since 3.2.2 this method is deprecated. Use {@link #messageReceiveFailed(MessageReceiveFailureEvent)}
     *
     * This method gets called when an incoming message associated with a Mode.PUSH plugin and an associated
     * PMode[1].errorHandling.Report.ProcessErrorNotifyConsumer=true is rejected by the MSH. The error details
     * are provided by #getErrorsForMessage
     *
     * @param messageId the Id of the failed message
     * @param ednpoint  the endpoint that tried to send the message or null if unknown
     */
    @Deprecated
    void messageReceiveFailed(String messageId, String ednpoint);

    /**
     * This method gets called when an incoming message is rejected by the MSH
     * @param messageReceiveFailureEvent event containing details about the message receive failure event
     */
    void messageReceiveFailed(MessageReceiveFailureEvent messageReceiveFailureEvent);

    /**
     * This method gets called when the status of a User Message changes
     * @param event event containing details about the message status change event
     */
    void messageStatusChanged(MessageStatusChangeEvent event);

    /**
     * This method gets called when an outgoing message associated with a Mode.PUSH plugin and an associated
     * PMode[1].errorHandling.Report.ProcessErrorNotifyProducer=true has finally failed to be delivered. The error details
     * are provided by #getErrorsForMessage. This is only called for messages that have no rerty attempts left.
     *
     * @param messageId the Id of the failed message
     */
    void messageSendFailed(String messageId);

    /**
     * This method gets called when an outgoing message associated with a Mode.PUSH plugin has been successfully sent
     * to the intended receiving MSH
     *
     * @param messageId the Id of the successful message
     */
    void messageSendSuccess(String messageId);

    /**
     * Describes the behaviour of the plugin regarding message delivery to the backend application
     */
    enum Mode {
        /**
         * Messages and notifications are actively pushed to the backend application (i.e. via a JMS queue)
         */
        PUSH("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/push"),
        /**
         * Messages and notifications are actively pulled by the backend application (i.e. via a webservice)
         */
        PULL("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/pull");

        private final String fileMapping;
        Mode(String fileMapping) {
            this.fileMapping=fileMapping;
        }
        public String getFileMapping() {
            return fileMapping;
        }
    }

    /**
     * Describes the message exchange protocol
     */
    enum Mep {
        /**
         * Exchange is ONE WAY. Only one message user exchanged.
         */
        ONE_WAY("oneway"),
        /**
         * Exchange of multiple UserMessage.
         */
        TWO_WAY("twoway");


        private final String fileMapping;
        Mep(String fileMapping) {
           this.fileMapping = fileMapping;
        }

        public String getFileMapping() {
            return fileMapping;
        }
    }
}
