package eu.domibus.core.metrics;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import eu.domibus.api.metrics.Metrics;

public class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {
    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        return Metrics.HEALTH_CHECK_REGISTRY;
    }

}
