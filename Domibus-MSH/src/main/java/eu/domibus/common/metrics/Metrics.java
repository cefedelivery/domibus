package eu.domibus.common.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class Metrics {

    private Metrics() {
    }

    public static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();

    public static final HealthCheckRegistry HEALTH_CHECK_REGISTRY = new HealthCheckRegistry();

}