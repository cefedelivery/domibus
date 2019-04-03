package eu.domibus.common.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.jvm.CachedThreadStatesGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Configuration
public class MetricsConfiguration {

    protected static final Logger LOG = LoggerFactory.getLogger(MetricsConfiguration.class);

    protected static final String DOMIBUS_METRICS_JMX_REPORTER_ENABLE = "domibus.metrics.jmx.reporter.enable";

    protected static final String DOMIBUS_METRICS_SL4J_REPORTER_ENABLE = "domibus.metrics.sl4j.reporter.enable";

    protected static final String DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_NUMBER = "domibus.metrics.sl4j.reporter.period.number";

    protected static final String DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_TIME_UNIT = "domibus.metrics.sl4j.reporter.period.time.unit";

    protected static final Marker STATISTIC_MARKER = MarkerFactory.getMarker("STATISTIC");

    protected static final String DOMIBUS_METRICS_MONITOR_MEMORY = "domibus.metrics.monitor.memory";

    protected static final String DOMIBUS_METRICS_MONITOR_GC = "domibus.metrics.monitor.gc";

    protected static final String DOMIBUS_METRICS_MONITOR_CACHED_THREADS = "domibus.metrics.monitor.cached.threads";

    @Bean
    public HealthCheckRegistry healthCheckRegistry() {
        return new HealthCheckRegistry();
    }

    @Bean
    public MetricRegistry metricRegistry(DomibusPropertyProvider domibusPropertyProvider) {
        MetricRegistry metricRegistry = new MetricRegistry();
        Boolean monitorMemory = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_MEMORY);

        Boolean monitorGc = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_GC);

        Boolean monitorCachedThread = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_MONITOR_CACHED_THREADS);

        if (monitorMemory) {
            metricRegistry.register("memory", new MemoryUsageGaugeSet());
        }

        if (monitorGc) {
            metricRegistry.register("gc", new GarbageCollectorMetricSet());
        }
        if (monitorCachedThread) {
            metricRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
        }

        Boolean jmxReporterEnabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_JMX_REPORTER_ENABLE);
        if (jmxReporterEnabled) {
            LOG.info("Jmx metrics reporter enabled");
            JmxReporter jmxReporter = JmxReporter.forRegistry(metricRegistry).build();
            jmxReporter.start();
        }

        Boolean sl4jReporterEnabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_SL4J_REPORTER_ENABLE);
        if (sl4jReporterEnabled) {
            Integer periodProperty = domibusPropertyProvider.getIntegerProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_NUMBER);
            String timeUnitProperty = domibusPropertyProvider.getProperty(DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_TIME_UNIT);
            TimeUnit timeUnit = TimeUnit.MINUTES;
            try {

                TimeUnit configuredTimeUnit = TimeUnit.valueOf(timeUnitProperty);
                switch (configuredTimeUnit) {
                    case SECONDS:
                    case MINUTES:
                    case HOURS:
                        timeUnit = configuredTimeUnit;
                        break;
                    default:
                        LOG.warn("Unsupported time unit property:[{}],setting default to MINUTE", timeUnitProperty);
                }
            } catch (IllegalArgumentException e) {
                LOG.warn("Invalid time unit property:[{}],setting default to MINUTE", timeUnitProperty, e);
            }
            LOG.info("Sl4j metrics reporter enabled wit reporting time unit:[{}] and period:[{}]", timeUnit, periodProperty);
            final Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                    .outputTo(LoggerFactory.getLogger("eu.domibus.statistic"))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .markWith(STATISTIC_MARKER)
                    .build();
            reporter.start(periodProperty, timeUnit);
        }
        return metricRegistry;
    }

}
