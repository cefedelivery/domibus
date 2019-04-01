package eu.domibus.common.metrics;

import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.jmx.JmxReporter;
import eu.domibus.api.property.DomibusPropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            LOG.info("Jmx metrics reporter enabled");
            final Slf4jReporter reporter = Slf4jReporter.forRegistry(Metrics.METRIC_REGISTRY)
                    .outputTo(LoggerFactory.getLogger("com.example.metrics"))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .build();
            reporter.start(1, TimeUnit.MINUTES);
        }
    }

}
