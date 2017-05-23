package eu.domibus.web.rest.ro;

import eu.domibus.api.jms.JMSDestination;

import java.util.Map;

/**
 * Created by musatmi on 15/05/2017.
 */
public class DestinationsResponseRO {

    public Map<String, JMSDestination> getJmsDestinations() {
        return jmsDestinations;
    }

    public void setJmsDestinations(Map<String, JMSDestination> jmsDestinations) {
        this.jmsDestinations = jmsDestinations;
    }

    private Map<String, JMSDestination> jmsDestinations;
}
