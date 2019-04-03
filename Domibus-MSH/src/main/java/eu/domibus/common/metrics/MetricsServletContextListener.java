package eu.domibus.common.metrics;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class MetricsServletContextListener extends MetricsServlet.ContextListener {

    @Autowired
    private MetricRegistry metricRegistry;
    @Override
    protected MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
