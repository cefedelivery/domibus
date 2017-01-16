package eu.domibus.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Aspect responsible for cleaning the MDC keys after the execution of a method.
 * The MDC keys are cleaned only if before the method execution were not already set.
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
@Aspect
public class MDCCleanAspect {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MDCCleanAspect.class);

    @Around(value = "execution(public * eu.domibus..*(..)) && @annotation(MDCKey)")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        final String targetLocation = getTargetLocation(method);
        LOG.debug("Preparing to execute method [{}]", targetLocation);

        List<String> mdcKeysToClean = null;
        try {
            mdcKeysToClean = getMDCKeysToClean(targetLocation, method);
            final Object proceed = joinPoint.proceed();
            return proceed;
        } finally {
            LOG.debug("Finished executing method [{}]", targetLocation);
            cleanMDCKeys(targetLocation, mdcKeysToClean);
        }
    }

    protected List<String> getMDCKeysToClean(String locationIdentifier, Method method) {
        final List<String> candidatesMDCKeysToClean = getMDCKeyAnnotations(method);
        if (candidatesMDCKeysToClean == null) {
            return null;
        }
        LOG.debug("[{}]: found candidate MDC keys for cleaning [{}]", locationIdentifier, candidatesMDCKeysToClean);
        final List<String> mdcKeysToClean = getMDCKeysToClean(candidatesMDCKeysToClean);
        if (mdcKeysToClean.size() > 0) {
            LOG.debug("[{}]: the MDC keys [{}] will be cleaned", locationIdentifier, mdcKeysToClean);
        }
        return mdcKeysToClean;
    }

    protected String getTargetLocation(Method method) {
        return method.toString();
    }

    protected List<String> getMDCKeyAnnotations(Method method) {
        final MDCKey annotation = method.getAnnotation(MDCKey.class);
        if (annotation == null) {
            LOG.debug("No annotation present on method [{}]", method);
            return null;
        }
        return new ArrayList<>(Arrays.asList(annotation.value()));
    }

    protected List<String> getMDCKeysToClean(List<String> candidatesMDCKeysToClean) {
        List<String> result = new ArrayList<>();
        for (String key : candidatesMDCKeysToClean) {
            //if the MDC key was not previously set then we need to clean it
            if (LOG.getMDC(key) == null) {
                result.add(key);
            }
        }
        return result;
    }

    protected void cleanMDCKeys(String locationIdentifier, List<String> keysToClean) {
        if (keysToClean == null) {
            LOG.debug("[{}]: no MDC keys to clean", locationIdentifier);
            return;
        }
        for (String key : keysToClean) {
            LOG.debug("[{}]: removing MDC key [{}]", locationIdentifier, key);
            LOG.removeMDC(key);
        }
    }
}