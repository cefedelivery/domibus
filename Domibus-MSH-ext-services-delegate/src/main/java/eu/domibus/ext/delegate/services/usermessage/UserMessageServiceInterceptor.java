package eu.domibus.ext.delegate.services.usermessage;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.UserMessageException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
@Aspect
@Component
public class UserMessageServiceInterceptor extends ServiceInterceptor{

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageServiceInterceptor.class);

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.usermessage.UserMessageServiceDelegate.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        return new UserMessageException(e);
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}
