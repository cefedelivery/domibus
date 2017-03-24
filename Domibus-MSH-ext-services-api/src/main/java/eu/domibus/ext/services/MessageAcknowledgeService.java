package eu.domibus.ext.services;

import eu.domibus.ext.domain.MessageAcknowledgementDTO;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Service used to register when a message is delivered from C3 to the backend and the other way around, from the backend to C3.
 * <p>
 * <p>Here are the typical use cases for using the {@link MessageAcknowledgeService} : </p>
 * <p>
 * <ul>
 * <li>a message is received at C3 from C2: the plugin that handles the message registers an acknowledgment before delivering the message to the backend</li>
 * <li>a message is processed by the backend and it informs C3 via the plugin; the plugin registers an acknowledgment</li>
 * <li>a message is processed by the backend and informs C3 directly via the REST service; a REST service is exposed containing the same signature as {@link MessageAcknowledgeService}
 * and delegates the processing to {@link MessageAcknowledgeService}</li>
 * </ul>
 * <p>
 * <p>There are two ways of performing message acknowledgments from the backend to C3:</p>
 * <p>
 * <ul>
 * <li>synchronous</li>
 * <p>C3 notifies the backend synchronously and the backend process the messages also synchronously. In this case there is no need for the backend to send a separate message acknowledgement so
 * at the C3 side a message acknowledgment coming from the backend must be registered.</p>
 * <p>Eg: <br/>
 * BackendResponse backendResponse = plugin.callBackendWS(message) <br/>
 * messageAcknowledgeService.acknowledgeMessage(message.getId(), new Timestamp(System.currentTimeMillis()), "C3", "default-ws-plugin", new HashMap()) <br/>
 * messageAcknowledgeService.acknowledgeMessage(message.getId(), new Timestamp(System.currentTimeMillis()), "default-ws-plugin", "C3", new HashMap()) <br/>
 * </p>
 * <li>asynchronous</li>
 * <p>C3 notifies the backend synchronously/synchronously and the backend process the messages asynchronously. In this case the backend will send a separate message acknowledgement when
 * it manages to process the message successfully.</p>
 * <p>Eg: <br/>
 * plugin.sendMessageToTheBackend(message) <br/>
 * messageAcknowledgeService.acknowledgeMessage(message.getId(), new Timestamp(System.currentTimeMillis()), "C3", "default-jms-plugin", new HashMap()) <br/><br/>
 * <p>
 * //the default-jms-plugin receives a JMS message from the backend after the backend processed the message successfully
 * messageAcknowledgeService.acknowledgeMessage(message.getId(), new Timestamp(System.currentTimeMillis()), "default-jms-plugin", "C3", new HashMap()) <br/>
 * </p>
 * </ul>
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface MessageAcknowledgeService {

    /**
     * Registers an acknowledgment for a specific message using the provided properties
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @param from                 The source of the acknowledgement(eg: C3 or backend name)
     * @param to                   The target of the acknowledgement(eg: C3 or backend name)
     * @param properties           Custom properties of the message acknowledgment
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    MessageAcknowledgementDTO acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to, Map<String, String> properties) throws MessageAcknowledgeException;


    /**
     * Registers an acknowledgment for a specific message
     *
     * @param messageId            The message id for which the acknowledgement is registered
     * @param acknowledgeTimestamp Timestamp of the acknowledged time
     * @param from                 The source of the acknowledgement(eg: C3 or backend name)
     * @param to                   The target of the acknowledgement(eg: C3 or backend name)
     * @return The newly created message acknowledgement
     * @throws MessageAcknowledgeException Raised in case an exception occurs while trying to register an acknowledgment
     */
    MessageAcknowledgementDTO acknowledgeMessage(String messageId, Timestamp acknowledgeTimestamp, String from, String to) throws MessageAcknowledgeException;

    /**
     * Gets all acknowledgments associated to a message id
     *
     * @param messageId The message id for which message acknowledgments are retrieved
     * @return All acknowledgments registered for a specific message
     */
    List<MessageAcknowledgementDTO> getAcknowledgedMessages(String messageId) throws MessageAcknowledgeException;
}