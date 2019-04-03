package eu.domibus.common.metrics;

import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jmx.JmxReporter;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Configuration
public class MetricsConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsConfiguration.class);

    private static final String DOMIBUS_METRICS_JMX_REPORTER_ENABLE = "domibus.metrics.jmx.reporter.enable";

    private static final String DOMIBUS_METRICS_SL4J_REPORTER_ENABLE = "domibus.metrics.sl4j.reporter.enable";

    public static final String DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_NUMBER = "domibus.metrics.sl4j.reporter.period.number";

    public static final String DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_TIME_UNIT = "domibus.metrics.sl4j.reporter.period.time.unit";

    public static final Marker STATISTIC_MARKER = MarkerFactory.getMarker("STATISTIC");

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @PostConstruct
    public void init() {
        Boolean jmxReporterEnabled = domibusPropertyProvider.getBooleanProperty(DOMIBUS_METRICS_JMX_REPORTER_ENABLE);
        if (jmxReporterEnabled) {
            LOG.info("Jmx metrics reporter enabled");
            JmxReporter jmxReporter = JmxReporter.forRegistry(Metrics.METRIC_REGISTRY).build();
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
            final Slf4jReporter reporter = Slf4jReporter.forRegistry(Metrics.METRIC_REGISTRY)
                    .outputTo(LoggerFactory.getLogger("eu.domibus.statistic"))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .markWith(STATISTIC_MARKER)
                    .build();
            reporter.start(periodProperty, timeUnit);
        }
    }

}
