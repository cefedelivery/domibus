package eu.domibus.ext.delegate.services.interceptor;

import eu.domibus.api.util.AOPUtil;
import eu.domibus.ext.exceptions.DomibusServiceExtException;
import eu.domibus.logging.DomibusLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class ServiceInterceptor {


    @Autowired
    AOPUtil aopUtil;

    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        final String methodSignature = aopUtil.getMethodSignature(joinPoint);
        getLogger().debug("Preparing to execute method [{}]", methodSignature);

        try {
            final Object proceed = joinPoint.proceed();
            return proceed;
        } catch (DomibusServiceExtException e) {
            //nothing to convert; re-throw exception
            throw e;
        } catch (Exception e) {
            //converts internal(core) exceptions to the exceptions exposed by the ext API
            throw convertCoreException(e);
        } finally {
            getLogger().debug("Finished executing method [{}]", methodSignature);
        }
    }

    public abstract Exception convertCoreException(Exception e);

    public abstract DomibusLogger getLogger();
}