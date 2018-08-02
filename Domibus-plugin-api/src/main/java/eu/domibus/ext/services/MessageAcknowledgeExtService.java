package eu.domibus.ext.services;

import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.exceptions.AuthenticationExtException;
import eu.domibus.ext.exceptions.MessageAcknowledgeExtException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Service used to acknowledge when a message is:
 * <ul>
 *     <li>delivered from C3 to the backend</li>
 *     <li>processed by the backend</li>
 * </ul>
 * <p>Here are the typical use cases for using the {@link MessageAcknowledgeExtService} : </p>
 * <ul>
 * <li>a message is received by C3 from C2: the plugin that handles the message registers an acknowledgment before delivering the message to the backend</li>
 * <li>a message is processed by the backend and it informs C3 via the plugin; the plugin registers an acknowledgment that the message has been processed by the backend</li>
 * <li>a message is processed by the backend and informs C3 directly via the REST service exposed by the core; a REST service is exposed containing the same signature as {@link MessageAcknowledgeExtService}</li>
 * </ul>
 * <p>There are two ways of performing message acknowledgments between C3 and the backend:</p>
 * <ul>
 * <li>synchronous
 * <p>C3(via the plugin) notifies the backend synchronously and the backend process the messages also synchronously. In this case there is no need for the backend to send a separate message acknowledgement so
 * the plugin at the C3 side registers the processing of the message by the backend.</p>
 * <p>Eg: <br>
 * BackendResponse backendResponse = plugin.callBackendWS(message) <br>
 * messageAcknowledgeService.acknowledgeMessageDelivered(message.getId(), new Timestamp(System.currentTimeMillis())) <br>
 * messageAcknowledgeService.acknowledgeMessageProcessed(message.getId(), new Timestamp(System.currentTimeMillis())) <br>
 * </p>
 * <li>asynchronous
 * <p>C3 notifies the backend synchronously and the backend process the messages asynchronously. In this case the backend will send a separate message acknowledgement when
 * it manages to process the message successfully.</p>
 * <p>Eg: <br>
 * plugin.sendMessageToTheBackend(message) <br>
 * messageAcknowledgeService.acknowledgeMessageDelivered(message.getId(), new Timestamp(System.currentTimeMillis())) <br><br>
 * <p>
 * //the plugin receives a JMS message from the backend after the backend processed the message successfully
 * messageAcknowledgeService.acknowledgeMessageProcessed(message.getId(), new Timestamp(System.currentTimeMillis())) <br>
 * </p>
 * </ul>
 *
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface MessageAcknowledgeExtService {

    /**
     * Acknowledges that a message has been delivered to the backend
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @param properties           Custom properties of the message acknowledgment
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeExtException Raised in case an exception occurs while trying to register an acknowledgment
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated or the user does not have the permission to access the message
     */
    MessageAcknowledgementDTO acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws AuthenticationExtException, MessageAcknowledgeExtException;

    /**
     * Acknowledges that a message has been delivered to the backend
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeExtException Raised in case an exception occurs while trying to register an acknowledgment
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated or the user does not have the permission to access the message
     */
    MessageAcknowledgementDTO acknowledgeMessageDelivered(String messageId, Timestamp acknowledgeTimestamp) throws AuthenticationExtException, MessageAcknowledgeExtException;


    /**
     * Acknowledges that a message has been processed by the backend
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @param properties           Custom properties of the message acknowledgment
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeExtException Raised in case an exception occurs while trying to register an acknowledgment
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated or the user does not have the permission to access the message
     */
    MessageAcknowledgementDTO acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp, Map<String, String> properties) throws AuthenticationExtException, MessageAcknowledgeExtException;


    /**
     * Acknowledges that a message has been processed by the backend
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeExtException Raised in case an exception occurs while trying to register an acknowledgment
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated or the user does not have the permission to access the message
     */
    MessageAcknowledgementDTO acknowledgeMessageProcessed(String messageId, Timestamp acknowledgeTimestamp) throws AuthenticationExtException, MessageAcknowledgeExtException;

    /**
     * Gets all acknowledgments associated to a message id
     *
     * @param messageId The message id for which message acknowledgments are retrieved
     * @return All acknowledgments registered for a specific message
     * @throws MessageAcknowledgeExtException Raised in case an exception occurs while trying to get the acknowledgments
     * @throws AuthenticationExtException Raised in case the security is enabled and the user is not authenticated or the user does not have the permission to access the message
     */
    List<MessageAcknowledgementDTO> getAcknowledgedMessages(String messageId) throws AuthenticationExtException, MessageAcknowledgeExtException;
}