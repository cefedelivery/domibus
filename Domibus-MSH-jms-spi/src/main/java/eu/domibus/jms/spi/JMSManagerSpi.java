package eu.domibus.jms.spi;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
public interface JMSManagerSPI {

    Map<String, JMSDestinationSPI> getDestinations();

    boolean sendMessage(JmsMessageSPI message, String destination);

    boolean deleteMessages(String source, String[] messageIds);

    boolean moveMessages(String source, String destination, String[] messageIds);

    JmsMessageSPI getMessage(String source, String messageId);

    List<JmsMessageSPI> getMessages(String source, String jmsType, Date fromDate, Date toDate, String selector);
}
