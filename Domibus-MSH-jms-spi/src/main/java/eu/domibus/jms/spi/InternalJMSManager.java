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

    String QUEUE = "Queue";

    /** in multi-tenancy mode domain admins should not see any count of messages so we set this value */
    long NB_MESSAGES_ADMIN = -1L;

    Map<String, InternalJMSDestination> findDestinationsGroupedByFQName();

    void sendMessage(InternalJmsMessage message, String destination);

    void sendMessage(InternalJmsMessage message, Destination destination);

    void deleteMessages(String source, String[] messageIds);

    void moveMessages(String source, String destination, String[] messageIds);

    InternalJmsMessage getMessage(String source, String messageId);

    List<InternalJmsMessage> browseMessages(String source, String jmsType, Date fromDate, Date toDate, String selector);

    InternalJmsMessage consumeMessage(String source, String customMessageId);
}
