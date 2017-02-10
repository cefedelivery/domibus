package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JMSDestination;
import eu.domibus.jms.spi.InternalJMSDestination;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * // TODO Documentation
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
public class JMSDestinationMapper {

    public Map<String, List<JMSDestination>> convert(Map<String, List<InternalJMSDestination>> destinations) {
        Map<String, List<JMSDestination>> result = new HashMap<>();
        for (Map.Entry<String, List<InternalJMSDestination>> spiEntry : destinations.entrySet()) {
            result.put(spiEntry.getKey(), convert(spiEntry.getValue()));
        }
        return result;
    }

    public List<JMSDestination> convert(List<InternalJMSDestination> internalJmsDestinations) {
        List<JMSDestination> dests = new ArrayList<>();
        for (InternalJMSDestination internalJmsDestination : internalJmsDestinations) {
            dests.add(convert(internalJmsDestination));
        }
        return dests;
    }

    public JMSDestination convert(InternalJMSDestination internalJmsDestination) {
        JMSDestination result = new JMSDestination();
        result.setType(internalJmsDestination.getType());
        result.setName(internalJmsDestination.getName());
        result.setNumberOfMessages(internalJmsDestination.getNumberOfMessages());
        result.setInternal(internalJmsDestination.isInternal());
        result.setProperties(internalJmsDestination.getProperties());
        return result;
    }
}
