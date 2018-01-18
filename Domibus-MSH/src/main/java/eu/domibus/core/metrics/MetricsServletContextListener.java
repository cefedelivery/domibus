package eu.domibus.core.metrics;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import eu.domibus.api.metrics.Metrics;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

public class MetricsServletContextListener extends MetricsServlet.ContextListener{
    @Override
    protected MetricRegistry getMetricRegistry() {
        return Metrics.METRIC_REGISTRY;
    }
}

