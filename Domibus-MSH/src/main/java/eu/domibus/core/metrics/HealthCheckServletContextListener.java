package eu.domibus.core.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import eu.domibus.api.metrics.Metrics;


/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {
    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        return Metrics.HEALTH_CHECK_REGISTRY;
    }
}
