package eu.domibus.web.rest.ro;

import eu.domibus.api.jms.JMSDestination;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by musatmi on 15/05/2017.
 */
public class DestinationsResponseRO {
    private SortedMap<String, JMSDestination> jmsDestinations;

    public SortedMap<String, JMSDestination> getJmsDestinations() {
        return jmsDestinations;
    }

    public void setJmsDestinations(SortedMap<String, JMSDestination> destinations) {
        this.jmsDestinations = destinations;
    }
}
