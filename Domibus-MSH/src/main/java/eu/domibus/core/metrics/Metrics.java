package eu.domibus.core.metrics;

import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import eu.domibus.plugin.handler.DatabaseMessageHandler;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;
import static eu.domibus.api.metrics.Metrics.METRIC_REGISTRY;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class Metrics {

    private final static com.codahale.metrics.Timer globalTimer = METRIC_REGISTRY.register(name(DatabaseMessageHandler.class, "responses"),new Timer(new SlidingTimeWindowReservoir(2, TimeUnit.MINUTES)));
    public static Timer.Context getGlobalContext() {
        return globalTimer.time();
    }

}
