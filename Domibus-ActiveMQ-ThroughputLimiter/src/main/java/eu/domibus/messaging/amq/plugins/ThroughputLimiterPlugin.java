
package eu.domibus.messaging.amq.plugins;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerPlugin;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class ThroughputLimiterPlugin implements BrokerPlugin {

    private ThroughputFilter filter;
    private long resendDelay;

    public ThroughputLimiterPlugin(final ThroughputFilter filter) {
        this(filter, 10000);
    }

    public ThroughputLimiterPlugin(final ThroughputFilter filter, final long resendDelay) {
        this.filter = filter;
        this.resendDelay = resendDelay;
    }

    @Override
    public Broker installPlugin(final Broker broker) throws Exception {
        if (filter == null) {
            throw new IllegalStateException("No Filter defined for ThroughputLimiter Plugin");
        }
        return new ThroughputLimiter(broker, filter, resendDelay);
    }
}
