package eu.domibus.api.jms;

import javax.jms.Queue;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
public interface JMSManager {

    Map<String, JMSDestination> getDestinations();

    JmsMessage getMessage(String source, String messageId);

    List<JmsMessage> getMessages(String source, String jmsType, Date fromDate, Date toDate, String selector);

    //TODO throw exception instead of returning boolean
    boolean sendMessageToQueue(JmsMessage message, String destination);

    void sendMessageToQueue(JmsMessage message, Queue destination);

    boolean deleteMessages(String source, String[] messageIds);

    boolean moveMessages(String source, String destination, String[] messageIds);
}
