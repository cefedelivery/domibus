package eu.domibus.api.util;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface AOPUtil {

    String getMethodSignature(ProceedingJoinPoint joinPoint);
}
