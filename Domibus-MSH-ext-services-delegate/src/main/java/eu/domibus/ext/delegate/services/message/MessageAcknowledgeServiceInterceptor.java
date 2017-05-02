package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.util.AOPUtil;
import eu.domibus.ext.exceptions.DomibusServiceException;
import eu.domibus.ext.exceptions.MessageAcknowledgeException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MessageAcknowledgeServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageAcknowledgeServiceInterceptor.class);

    @Autowired
    AOPUtil aopUtil;

    @Around(value = "execution(public * eu.domibus.ext.delegate.services.message.MessageAcknowledgeServiceDelegate.*(..))")
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        final String methodSignature = aopUtil.getMethodSignature(joinPoint);
        LOG.debug("Preparing to execute method [{}]", methodSignature);

        try {
            final Object proceed = joinPoint.proceed();
            return proceed;
        } catch (DomibusServiceException e) {
            //nothing to convert; re-throw exception
            throw e;
        } catch (Exception e) {
            //converts internal(core) exceptions to the exceptions exposed by the ext API
            throw new MessageAcknowledgeException(e);
        } finally {
            LOG.debug("Finished executing method [{}]", methodSignature);
        }
    }
}