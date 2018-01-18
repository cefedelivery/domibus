package eu.domibus.api.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class Metrics {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(Metrics.class);

    public static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();
    public static final HealthCheckRegistry HEALTH_CHECK_REGISTRY = new HealthCheckRegistry();

}
