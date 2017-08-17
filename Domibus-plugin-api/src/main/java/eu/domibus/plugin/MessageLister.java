
package eu.domibus.plugin;

import eu.domibus.messaging.MessageNotFoundException;

import java.util.Collection;

/**
 * Provides access to messages waiting to be pulled by plugins using BackendConnector.Mode.PULL.
 *
 * @author Christian Koch, Stefan Mueller
 */
public interface MessageLister {

    /**
     * Lists all messages pending for download by the backend
     *
     * @return a collection of messageIds pending download
     */
    Collection<String> listPendingMessages();

    /**
     * removes the message with the corresponding id from the download queue
     *
     * @param messageId id of the message to be removed
     * @throws MessageNotFoundException if the message is not pending
     */
    void removeFromPending(String messageId) throws MessageNotFoundException;
}
