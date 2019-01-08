package eu.domibus.common.statistics;

import com.codahale.metrics.Counter;
import eu.domibus.api.metrics.Metrics;
import eu.domibus.ebms3.common.model.AbstractBaseAuditEntity;
import eu.domibus.ebms3.receiver.MSHWebservice;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import static com.codahale.metrics.MetricRegistry.name;
import static eu.domibus.logging.DomibusLogger.MDC_USER;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * In order to perform auditing, we are using hibernate envers to keep track of changes for most of our entities.
 * However in order to avoid degrading performance of the application, we do a simpler kind of auditing on entities that are participating in heavy
 * throughput (like messages). This aspect is in charge of this simple auditing mechanism.
 */
@Aspect
@Component
public class StatisticAspect {

    static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(StatisticAspect.class);

    @Around("@annotation(timer)")
    public Object profileMethod(ProceedingJoinPoint pjp, Timer timer) throws Throwable {
        com.codahale.metrics.Timer.Context context = null;
        final Class<?> clazz = timer.clazz();
        final String timerName = timer.timerName();
        LOG.trace("adding a timer with name:[{}] in class:[{}]", timerName, clazz.getName());
        com.codahale.metrics.Timer methodTimer = Metrics.METRIC_REGISTRY.timer(name(clazz, timerName+"_timer"));
        Counter methodCounter = Metrics.METRIC_REGISTRY.counter(name(MSHWebservice.class, timerName+"_counter"));
        try {
            context = methodTimer.time();
            methodCounter.inc();
            return pjp.proceed();
        } finally {
            if (context != null) {
                context.stop();
            }
            if(methodCounter!=null){
                methodCounter.dec();
            }
        }
    }
}
