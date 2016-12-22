package eu.domibus.jms.spi;

import javax.jms.Queue;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
public interface InternalJMSManager {

    Map<String, InternalJMSDestination> getDestinations();

    void sendMessage(InternalJmsMessage message, String destination);

    void sendMessage(InternalJmsMessage message, Queue destination);

    void deleteMessages(String source, String[] messageIds);

    void moveMessages(String source, String destination, String[] messageIds);

    InternalJmsMessage getMessage(String source, String messageId);

    List<InternalJmsMessage> getMessages(String source, String jmsType, Date fromDate, Date toDate, String selector);
}
