package eu.domibus.tomcat.activemq;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Properties;

public class DomibusBrokerFactoryBean extends BrokerFactoryBean {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusBrokerFactoryBean.class);

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Override
    public Object getObject() throws Exception {
        String maxCountPendingMessagesStr = domibusProperties.getProperty("domibus.listPendingMessages.maxCount");
        ActiveMQDestination[] destinations = this.getBroker().getDestinations();
        final int maxBrowsePageSize = Integer.parseInt(maxCountPendingMessagesStr);
        for (ActiveMQDestination activeMQDestination : destinations) {
            final Destination destination = this.getBroker().getDestination(activeMQDestination);
            if(destination != null) {
                destination.setMaxBrowsePageSize(maxBrowsePageSize);
                LOGGER.debug("MaxBrowsePageSize was set to [{}] in [{}]", maxCountPendingMessagesStr, activeMQDestination);
            }
        }
        return this.getBroker();
    }
}
