package eu.domibus.jms.spi;

import javax.jms.Destination;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * // TODO Documentation
 *
 * @author Cosmin Baciu
 * @since 3.2
 */
public interface InternalJMSManager {

    static final String QUEUE = "Queue";

    static final String TOPIC = "Topic";

    Map<String, InternalJMSDestination> findDestinationsGroupedByFQName();

    void sendMessage(InternalJmsMessage message, String destination);

    void sendMessage(InternalJmsMessage message, Destination destination);

    void deleteMessages(String source, String[] messageIds);

    void moveMessages(String source, String destination, String[] messageIds);

    InternalJmsMessage getMessage(String source, String messageId);

    List<InternalJmsMessage> browseMessages(String source, String jmsType, Date fromDate, Date toDate, String selector);

    List<InternalJmsMessage> browseMessages(String source);

    InternalJmsMessage consumeMessage(String source, String customMessageId);
}
