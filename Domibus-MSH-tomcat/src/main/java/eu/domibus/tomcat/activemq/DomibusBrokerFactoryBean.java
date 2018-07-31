package eu.domibus.tomcat.activemq;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.activemq.broker.region.Destination;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.xbean.BrokerFactoryBean;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  This factory bean overrides getObject method just for setting MaxBrowsePageSize as the
 *  maxCount of listPendingMessages
 *
 * @author Tiago Miguel
 * @since 3.3.2
 */
public class DomibusBrokerFactoryBean extends BrokerFactoryBean {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusBrokerFactoryBean.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public Object getObject() throws Exception {
        String maxCountPendingMessagesStr = domibusPropertyProvider.getProperty("domibus.listPendingMessages.maxCount");
        ActiveMQDestination[] destinations = this.getBroker().getDestinations();
        final int maxBrowsePageSize = NumberUtils.toInt(maxCountPendingMessagesStr);
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
