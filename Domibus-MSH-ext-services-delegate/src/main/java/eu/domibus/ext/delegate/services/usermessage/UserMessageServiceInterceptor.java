package eu.domibus.ext.delegate.services.usermessage;

import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.ext.exceptions.UserMessageException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;

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
