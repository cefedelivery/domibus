package eu.domibus.ext.delegate.services.message;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.MessageExtException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Sebastian-Ion TINCU
 */
@Aspect
@Component
public class MessageServiceInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageServiceInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.message.MessageServiceImpl.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        return new MessageExtException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}
