package eu.domibus.core.replication;

/**
 * Replication Data service interface - all operations of writing data into {@code TB_MESSAGE_UI} table
 *
 * @author Catalin Enache
 * @since 4.0
 */
public interface UIReplicationDataService {

    /**
     * replicates data on receiver side when a new user message is received
     *
     * @param messageId
     */
    void messageReceived(final String messageId);

    /**
     * replicates data on sender side when a new user message is submitted
     *
     * @param messageId
     */
    void messageSubmitted(final String messageId);

    /**
     * updates/sync data on receiver/sender side when a change in messages status appears
     *  @param messageId
     *
     */
    void messageStatusChange(final String messageId);

    /**
     * updates/sync data on receiver/sender side when a change in messages notification status appears
     *  @param messageId
     *
     */
    void messageNotificationStatusChange(final String messageId);

    void messageChange(final String messageId);

    /**
     * replicates data on sender side when a new signal message is submitted
     *
     * @param messageId
     */
    void signalMessageSubmitted(final String messageId);

    /**
     * replicates data on receiver side when a new signal message is received
     *
     * @param messageId
     */
    void signalMessageReceived(final String messageId);

    /**
     * run the diff query against {@code V_MESSAGE_UI} view and sync the data
     */
    void findAndSyncUIMessages();

}
