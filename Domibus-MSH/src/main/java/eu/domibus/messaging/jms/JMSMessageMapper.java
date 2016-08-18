package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.jms.spi.JmsMessageSPI;
import org.springframework.stereotype.Component;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
@Component
public class JMSMessageMapper {

    public JmsMessageSPI convert(JmsMessage message) {
        JmsMessageSPI result = new JmsMessageSPI();
        result.setId(message.getId());
        result.setContent(message.getContent());
        result.setTimestamp(message.getTimestamp());
        result.setType(message.getType());
        result.setProperties(message.getProperties());
        return result;
    }
}
