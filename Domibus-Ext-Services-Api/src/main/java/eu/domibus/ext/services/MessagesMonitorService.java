package eu.domibus.ext.services;

import eu.domibus.ext.services.domain.AttemptDTO;
import eu.domibus.ext.services.exceptions.MessagesMonitorException;

import java.util.List;

/**
 * This interface exposes all the available operations related to the messages monitor service.
 *
 * @author Federico Martini
 * @since 3.3
 */
public interface MessagesMonitorService {

    /**
     * Operation to retrieve all the messages that are currently in a SEND_FAILURE status in the access point.
     *
     * @return List list of unique message ids.
     * @throws MessagesMonitorException
     */
    public List<String> getFailedMessages() throws MessagesMonitorException;

    /**
     * Operation to get the time, in msecs, that a message has been in a SEND_FAILURE status in the access point.
     *
     * @param messageId Unique id of the message
     * @return long
     * @throws MessagesMonitorException
     */
    public long getSendFailureMessageTime(String messageId) throws MessagesMonitorException;

    /**
     * Operation to unblock and retry to send a message which has a SEND_FAILURE status in the access point.
     * The retry mechanism, as configured in the PMode, will apply.
     *
     * @param messageId Unique id of the message
     * @return boolean
     * @throws MessagesMonitorException
     */
    public boolean restoreFailedMessage(String messageId) throws MessagesMonitorException;

    /**
     * Operation to delete a message which is in SEND_FAILURE status in the access point.
     *
     * @param messageId Unique id of the message
     * @return boolean
     * @throws MessagesMonitorException
     */
    public boolean deleteFailedMessage(String messageId) throws MessagesMonitorException;

    /**
     * Operation to get the history of the delivery attempts for a certain message.
     *
     * @param messageId Unique id of the message
     * @return List
     * @throws MessagesMonitorException
     */
    public List<AttemptDTO> getAttemptsHistory(String messageId) throws MessagesMonitorException;

}