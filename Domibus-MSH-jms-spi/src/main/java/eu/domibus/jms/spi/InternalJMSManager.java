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

    boolean sendMessage(InternalJmsMessage message, String destination);

    void sendMessage(InternalJmsMessage message, Queue destination);

    boolean deleteMessages(String source, String[] messageIds);

    boolean moveMessages(String source, String destination, String[] messageIds);

    InternalJmsMessage getMessage(String source, String messageId);

    List<InternalJmsMessage> getMessages(String source, String jmsType, Date fromDate, Date toDate, String selector);
}
