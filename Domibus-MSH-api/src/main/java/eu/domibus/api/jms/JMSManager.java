package eu.domibus.api.jms;

import javax.jms.Queue;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
public interface JMSManager {

    Map<String, JMSDestination> getDestinations();

    JmsMessage getMessage(String source, String messageId);

    List<JmsMessage> getMessages(String source, String jmsType, Date fromDate, Date toDate, String selector);

    void sendMessageToQueue(JmsMessage message, String destination);

    void sendMessageToQueue(JmsMessage message, Queue destination);

    void deleteMessages(String source, String[] messageIds);

    void moveMessages(String source, String destination, String[] messageIds);
}
