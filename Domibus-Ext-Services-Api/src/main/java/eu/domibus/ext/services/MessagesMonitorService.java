package eu.domibus.ext.services;

import eu.domibus.ext.services.domain.AttemptDTO;

import java.util.List;

/**
 * This interface exposes all the available operations related to the messages monitor service.
 *
 * @author Federico Martini
 * @since 3.3
 */
public interface MessagesMonitorService {

    /**
     * Operation to retrieve all the messages that are currently in the Dead Letter Queue.
     *
     * @return List a list of message ids
     * @throws MessagesMonitorException
     */
    public List<String> getMessagesInDLQ() throws MessagesMonitorException;

    /**
     * Operation to get the time, in msecs, a message has been in the Dead Letter Queue.
     *
     * @param messageId
     * @return long
     * @throws MessagesMonitorException
     */
    public long getMessageInDLQTime(String messageId) throws MessagesMonitorException;

    /**
     * Operation to put back a message in the "normal sending" queue.
     *
     * @param messageId
     * @return boolean
     * @throws MessagesMonitorException
     */
    public boolean restoreMessageInDLQ(String messageId) throws MessagesMonitorException;

    /**
     * Operation to delete a message which is in the Dead Letter Queue.
     *
     * @param messageId
     * @return boolean
     * @throws MessagesMonitorException
     */
    public boolean deleteMessage(String messageId) throws MessagesMonitorException;

    /**
     * Operation to get the history of delivery attempts.
     *
     * @param messageId
     * @return List
     * @throws MessagesMonitorException
     */
    public List<AttemptDTO> getAttemptsHistory(String messageId) throws MessagesMonitorException;

}