package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.jms.spi.JmsMessageSPI;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    public JmsMessage convert(JmsMessageSPI message) {
        JmsMessage result = new JmsMessage();
        result.setId(message.getId());
        result.setContent(message.getContent());
        result.setTimestamp(message.getTimestamp());
        result.setType(message.getType());
        result.setProperties(message.getProperties());
        return result;
    }

    public List<JmsMessage> convert(List<JmsMessageSPI> messagesSPI) {
        List<JmsMessage> result = new ArrayList<>();
        for (JmsMessageSPI jmsMessageSPI : messagesSPI) {
            result.add(convert(jmsMessageSPI));
        }
        return result;
    }
}
