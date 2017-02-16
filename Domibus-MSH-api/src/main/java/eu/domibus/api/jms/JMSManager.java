package eu.domibus.api.jms;

import javax.jms.Queue;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * // TODO Documentation
 *
 * @author Cosmin Baciu
 * @since 3.2
 * @see JmsMessage
 * @see JMSDestination
 */
public interface JMSManager {

    /**
     * Operation to get all destinations available on the JMS server
     *
     * @return a map where the key is the fully qualified name of the real JMS destination and the value is a JMSDestination object.
     */
    Map<String, JMSDestination> getDestinations();

    /**
     * Finds a JMS message in a source.
     *
     * @param source    a JMS source
     * @param messageId JMS message's identifier
     * @return a JmsMessage
     */
    JmsMessage getMessage(String source, String messageId);

    /**
     * Operation to browse all messages in a JMS source.
     *
     * @param source queue or topic
     * @return a list of JmsMessage
     */
    List<JmsMessage> browseMessages(String source);

    /**
     * Operation to browse a JMS source with restrictions given by the parameters.
     *
     * @param source   queue or topic
     * @param jmsType  type of the JMS message
     * @param fromDate starting date
     * @param toDate   ending date
     * @param selector selector
     * @return a list of JmsMessage
     */
    List<JmsMessage> browseMessages(String source, String jmsType, Date fromDate, Date toDate, String selector);

    void sendMessageToQueue(JmsMessage message, String destination);

    void sendMessageToQueue(JmsMessage message, Queue destination);

    void deleteMessages(String source, String[] messageIds);

    void moveMessages(String source, String destination, String[] messageIds);

    /**
     * Consumes a business message from a source.
     *
     * @param source    a JMS source
     * @param messageId Business message id
     * @return a JmsMessage
     */
    JmsMessage consumeMessage(String source, String messageId);
}
