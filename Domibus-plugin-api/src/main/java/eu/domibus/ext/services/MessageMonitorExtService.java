package eu.domibus.ext.services;

import eu.domibus.ext.domain.MessageAttemptDTO;
import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.exceptions.MessageMonitorExtException;

import java.util.Date;
import java.util.List;

/**
 * This interface exposes all the available operations related to the Messages Monitor Service.
 *
 * <p>The client can either call directly the REST service MessageMonitorResource or hook-up using this interface.
 *
 * <p>Assuming that "failed message" means failed to be sent by the sender access point and getting the status set to SEND_FAILURE, the service gives the possibility to:
 * <ul>
 * <li>list all the failed messages</li>
 * <li>restore a failed message</li>
 * <li>restore all messages failed during a specific period</li>
 * <li>know how long time a message has been failed</li>
 * <li>get the history of all delivery attempts</li>
 * <li>delete the message payload of a failed message</li>
 * </ul>
 *
 * <p>Notice about Domibus notification mechanism: whenever a message fails to be sent several times, according to the configuration of the reception awareness/retry in the PMode,
 * it's status gets changed to SEND_FAILURE and Domibus notifies the plugin via the void messageSendFailed(String messageId) method from the eu.domibus.plugin.BackendConnector.<br>
 * In case of the <b>default-jms-plugin</b> the method <b>messageSendFailed</b> is implemented to send a JMS message containing the error details on the "domibus.backend.jms.errorNotifyProducer" queue.</p>
 *
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface MessageMonitorExtService {

    /**
     * Operation to retrieve all the messages that are currently in a SEND_FAILURE status in the access point.
     * It takes into account the user permissions:
     * <ul>
     * <li>It returns all failed messages if unsecured authorization is allowed or the user has ROLE_ADMIN</li>
     * <li>It filter the messages based on the finalRecipient value in case the user has ROLE_USER</li>
     * </ul>
     *
     * @return List - a list of unique message ids.
     * @throws MessageMonitorExtException Raised in case an exception occurs while trying to get the failed messages list
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated
     */
    List<String> getFailedMessages() throws AuthenticationExtException, MessageMonitorExtException;


    /**
     * Operation to retrieve all the messages having the provided finalRecipient value that are currently in a SEND_FAILURE status in the access point.
     * It takes into account the user permissions:
     * <ul>
     * <li>It returns all failed messages if unsecured authorization is allowed or the user has ROLE_ADMIN</li>
     * <li>It filter the messages based on the finalRecipient value in case the user has ROLE_USER</li>
     * </ul>
     *
     * @param finalRecipient represents the destination
     * @return List - a list of unique message ids.
     * @throws MessageMonitorExtException Raised in case an exception occurs while trying to get the failed messages list
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated or the user does not have the permission to access messages having the provided finalRecipient
     */
    List<String> getFailedMessages(String finalRecipient) throws AuthenticationExtException, MessageMonitorExtException;

    /**
     * Operation to retrieve the time that a message has been in a SEND_FAILURE status in the access point.
     *
     * @param messageId Unique id of the message
     * @return long - the passed time in ms
     * @throws MessageMonitorExtException Raised in case an exception occurs while trying to get the failed message period
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated or the user does not have the permission to access the message
     */
    Long getFailedMessageInterval(String messageId) throws AuthenticationExtException, MessageMonitorExtException;

    /**
     * Operation to unblock and retry to send a message which has a SEND_FAILURE status in the access point.
     * <p>This operation will set the message to SEND_ENQUEUED status, so that a new send is attempted.
     * <p>Afterwards, in case of failure, the retry mechanism, as configured in the PMode, will apply.
     *
     * @param messageId Unique id of the message
     * @throws MessageMonitorExtException Raised in case an exception occurs while trying to restore the failed message
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated or the user does not have the permission to access the message
     */
    void restoreFailedMessage(String messageId) throws AuthenticationExtException, MessageMonitorExtException;

    /**
     * Operation to send a message which has a SEND_ENQUEUED status in the access point.
     *
     *
     * @param messageId Unique id of the message
     * @throws MessageMonitorExtException Raised in case an exception occurs while trying to send the message
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated or the user does not have the permission to access the message
     */
    void sendEnqueuedMessage(String messageId) throws AuthenticationExtException, MessageMonitorExtException;

    /**
     * Operation to unblock and retry to send all messages which had the SEND_FAILURE status
     * during the period occurred between the "begin" and "end" times.
     * <p>This operation will set each message to SEND_ENQUEUED status, so that a new send is attempted.
     * Afterwards, in case of failure, the retry mechanism, as configured in the PMode, will apply.</p>
     *
     * The method takes into account the user permissions:
     * <ul>
     * <li>It restores all failed messages matching the criteria if unsecured authorization is allowed or the user has ROLE_ADMIN</li>
     * <li>It restores the messages matching the finalRecipient value in case the user has ROLE_USER</li>
     * </ul>
     *
     * @param begin specific instant time starting period
     * @param end   specific instant time ending period
     * @return List the messages ids's list of successfully restored messages.
     * @throws MessageMonitorExtException Raised in case a blocking event occurs. It is not raised when the operation is successful for at least one message
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated
     */
    List<String> restoreFailedMessagesDuringPeriod(Date begin, Date end) throws AuthenticationExtException, MessageMonitorExtException;

    /**
     * Operation to delete a message which is in SEND_FAILURE status in the access point.
     * <p>Only the payload will be deleted. The LOGs tables will keep track of the message.
     *
     * @param messageId Unique id of the message
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated
     * @throws MessageMonitorExtException Raised in case an exception occurs while trying to delete the failed message
     */
    void deleteFailedMessage(String messageId) throws AuthenticationExtException, MessageMonitorExtException;

    /**
     * Operation to get the history of the delivery attempts for a certain message.
     *
     * @param messageId Unique id of the message
     * @return List - a list of MessageAttemptDTO
     * @throws MessageMonitorExtException Raised in case an exception occurs while trying to get the attempts history
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated or the user does not have the permission to access the message
     */
    List<MessageAttemptDTO> getAttemptsHistory(String messageId) throws AuthenticationExtException, MessageMonitorExtException;

}