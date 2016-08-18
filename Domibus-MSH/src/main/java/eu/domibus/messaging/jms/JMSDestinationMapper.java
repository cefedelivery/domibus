package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.jms.spi.JMSDestinationSPI;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cosmin Baciu on 17-Aug-16.
 */
@Component
public class JMSDestinationMapper {

    public Map<String, JMSDestination> convert(Map<String, JMSDestinationSPI> destinations) {
        Map<String, JMSDestination> result = new HashMap<>();
        for (Map.Entry<String, JMSDestinationSPI> spiEntry : destinations.entrySet()) {
            result.put(spiEntry.getKey(), convert(spiEntry.getValue()));
        }
        return result;
    }

    public JMSDestination convert(JMSDestinationSPI jmsDestinationSPI) {
        JMSDestination result = new JMSDestination();
        result.setType(jmsDestinationSPI.getType());
        result.setName(jmsDestinationSPI.getName());
        result.setNumberOfMessages(jmsDestinationSPI.getNumberOfMessages());
        result.setNumberOfMessagesPending(jmsDestinationSPI.getNumberOfMessagesPending());
        result.setProperties(jmsDestinationSPI.getProperties());
        return result;
    }
}
