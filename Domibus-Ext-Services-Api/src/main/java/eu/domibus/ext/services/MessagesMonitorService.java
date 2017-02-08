package eu.domibus.ext.services;

import eu.domibus.ext.services.domain.AttemptDTO;
import eu.domibus.ext.services.exceptions.MessagesMonitorException;

import java.util.Date;
import java.util.List;

/**
 * This interface exposes all the available operations related to the Messages Monitor Service.
 * <p>The client can either call directly the REST service MessageMonitorResource or hook-up using this interface.
 *
 * <p>Assuming that "failed message" means failed to be sent by the sender access point and getting the status set to SEND_FAILURE, the service gives the possibility:
 * <p>1. to list all the failed messages.
 * <p>2. to restore a failed message.
 * <p>3. to restore all messages failed during a specific period.
 * <p>4. to know how long time a message has been failed.
 * <p>5. to get the history of all delivery attempts.
 * <p>6. to delete the message payload of a failed message.
 *
 * <p>Notice about Domibus notification mechanism: whenever a message fails to be sent several times, according to the configuration of the reception awareness/retry in the PMode,
 * <p>it get the status changed to SEND_FAILURE and Domibus notifies the plugin(s) with a JMS message either on the "domibus.notification.webservice" or "domibus.notification.jms" queue.
 *
 * @author Federico Martini
 * @since 3.3
 */
public interface MessagesMonitorService {

    /**
     * Operation to retrieve all the messages that are currently in a SEND_FAILURE status in the access point.
     *
     * @return List - a list of unique message ids.
     * @throws MessagesMonitorException Raised in case an exception occurs while trying to get the failed messages list.
     */
    public List<String> getFailedMessages() throws MessagesMonitorException;

    /**
     * Operation to retrieve the time that a message has been in a SEND_FAILURE status in the access point.
     *
     * @param messageId Unique id of the message
     * @return long - the passed time in ms
     * @throws MessagesMonitorException Raised in case an exception occurs while trying to get the failed message period.
     */
    public Long getFailedMessageInterval(String messageId) throws MessagesMonitorException;

    /**
     * Operation to unblock and retry to send a message which has a SEND_FAILURE status in the access point.
     * <p>This will set the message to SEND_ENQUEUED status.
     * <p>Afterwards, in case of failure, the retry mechanism, as configured in the PMode, will apply.
     *
     * @param messageId Unique id of the message
     * @throws MessagesMonitorException Raised in case an exception occurs while trying to restore the failed message.
     */
    public void restoreFailedMessage(String messageId) throws MessagesMonitorException;

    /**
     * Operation to unblock and retry to send all messages which have the SEND_FAILURE status during the period occurring between the begin, end parameters.
     * <p>This will set each message to SEND_ENQUEUED status.
     * <p>Afterwards, in case of failure, the retry mechanism, as configured in the PMode, will apply.
     *
     * @param begin period start timestamp
     * @param end   period end timestamp
     * @return List the list of messages ids successfully restored
     * @throws MessagesMonitorException Raised in case an exception occurs
     */
    public List<String> restoreFailedMessagesDuringPeriod(Date begin, Date end) throws MessagesMonitorException;

    /**
     * Operation to delete a message which is in SEND_FAILURE status in the access point.
     * <p>Only the payload will be deleted. The LOGs tables will keep track of the message.
     *
     * @param messageId Unique id of the message
     * @throws MessagesMonitorException Raised in case an exception occurs while trying to delete the failed message.
     */
    public void deleteFailedMessage(String messageId) throws MessagesMonitorException;

    /**
     * Operation to get the history of the delivery attempts for a certain message.
     *
     * @param messageId Unique id of the message
     * @return List - a list of AttemptDTO
     * @throws MessagesMonitorException Raised in case an exception occurs while trying to get the attempts history.
     */
    public List<AttemptDTO> getAttemptsHistory(String messageId) throws MessagesMonitorException;

}