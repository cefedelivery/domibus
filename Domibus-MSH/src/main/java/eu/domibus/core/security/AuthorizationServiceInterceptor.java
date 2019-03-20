package eu.domibus.core.security;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.ext.delegate.services.interceptor.ServiceInterceptor;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Interceptor in charge of converting authentication spi exceptions into crypto exceptions.
 */
@Aspect
@Component
public class AuthorizationServiceInterceptor extends ServiceInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuthorizationServiceInterceptor.class);

    @Around(value = "execution(public * eu.domibus.core.security.AuthorizationService.*(..))")
    @Override
    public Object intercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.intercept(joinPoint);
    }

    @Override
    public Exception convertCoreException(Exception e) {
        if (e instanceof AuthorizationException) {
            AuthorizationException a = (AuthorizationException) e;
            LOG.trace("Converting Authorization exception:[{}] into EBMSException", e);
            if (a.getAuthorizationError() != null) {
                switch (a.getAuthorizationError()) {
                    case INVALID_FORMAT:
                        LOG.error("Invalid incoming message format during authorization:[{}]", a.getMessage());
                        return new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, a.getMessage(), a.getMessageId(), a.getCause());
                    case AUTHORIZATION_REJECTED:
                        LOG.error("Authorization for incoming message was not granted:[{}]", a.getMessage());
                        break;
                    case AUTHORIZATION_SYSTEM_DOWN:
                        LOG.error("Authorization system is down:[{}]", a.getMessage());
                        break;
                    case AUTHORIZATION_CONNECTION_REJECTED:
                        LOG.error("Connection credential to Authorization were rejecte:[{}]", a.getMessage());
                        break;
                }
            }
            return new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, a.getMessage(), a.getMessageId(), a.getCause());
        } else {
            return new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, e.getMessage(), null, e.getCause());
        }
    }

    @Override
    public DomibusLogger getLogger() {
        return LOG;
    }
}