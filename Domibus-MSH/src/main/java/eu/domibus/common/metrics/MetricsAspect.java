package eu.domibus.common.metrics;

import com.codahale.metrics.MetricRegistry;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * This aspect looks after framework annotation timer and counter, and then configure metrics. In this scenario,
 * drop wizard metrics.
 */
@Aspect
@Component
public class MetricsAspect {

    static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MetricsAspect.class);

    @Autowired
    private MetricRegistry metricRegistry;
    @Around("@annotation(timer)")
    public Object surroundWithATimer(ProceedingJoinPoint pjp, Timer timer) throws Throwable {
        com.codahale.metrics.Timer.Context context = null;
        final Class<?> clazz = timer.clazz();
        final String timerName = timer.value();
        LOG.trace("adding a timer with name:[{}] in class:[{}]", timerName, clazz.getName());
        com.codahale.metrics.Timer methodTimer = metricRegistry.timer(getMetricsName(clazz, timerName + "_timer"));
        try {
            context = methodTimer.time();
            return pjp.proceed();
        } finally {
            if (context != null) {
                context.stop();
            }
        }
    }

    @Around("@annotation(counter)")
    public Object surroundWithACounter(ProceedingJoinPoint pjp, Counter counter) throws Throwable {
        final Class<?> clazz = counter.clazz();
        final String counterName = counter.value();
        LOG.trace("adding a timer with name:[{}] in class:[{}]", counterName, clazz.getName());
        com.codahale.metrics.Counter methodCounter = metricRegistry.counter(getMetricsName(clazz, counterName + "_counter"));
        try {
            methodCounter.inc();
            return pjp.proceed();
        } finally {
            methodCounter.dec();
        }
    }

    protected String getMetricsName(final Class<?> clazz, final String timerName) {
        if (Default.class.isAssignableFrom(clazz)) {
            return timerName;
        } else {
            return name(clazz, timerName);
        }
    }
}
