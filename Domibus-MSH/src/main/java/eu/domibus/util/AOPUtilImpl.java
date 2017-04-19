package eu.domibus.util;

import eu.domibus.api.util.AOPUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class AOPUtilImpl implements AOPUtil {

    @Override
    public String getMethodSignature(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().toString();
    }
}
