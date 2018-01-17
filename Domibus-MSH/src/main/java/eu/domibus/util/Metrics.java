package eu.domibus.util;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import com.codahale.metrics.health.HealthCheckRegistry;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.handler.DatabaseMessageHandler;

import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class Metrics {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(Metrics.class);

    public static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();
    public static final HealthCheckRegistry HEALTH_CHECK_REGISTRY = new HealthCheckRegistry();

    private final static Meter requests = METRIC_REGISTRY.meter("requests");

    private final static com.codahale.metrics.Timer globalTimer = METRIC_REGISTRY.register(name(DatabaseMessageHandler.class, "responses"),new Timer(new SlidingTimeWindowReservoir(2, TimeUnit.MINUTES)));
    private static Timer.Context context;

    private static long lastTimeGlobalContextWasAsked = 0;

    public static com.codahale.metrics.Timer getGlobalTimer() {
        return globalTimer;
    }

    public static Timer.Context getGlobalContext() {
        //if ((System.currentTimeMillis() - lastTimeGlobalContextWasAsked) > 10000) {
       /* if(context==null){
            lastTimeGlobalContextWasAsked = System.currentTimeMillis();
            LOG.info("Reset global context at [{}]", lastTimeGlobalContextWasAsked);
            if (context != null) {
                context.close();
            }
            context = globalTimer.time();
        }
        return context;*/
       return globalTimer.time();
    }


}
