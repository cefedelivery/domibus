package eu.domibus.util;

import eu.domibus.api.util.ClassUtil;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@Component
public class ClassUtilImpl implements ClassUtil {

    @Override
    public String getTargetObjectClassCanonicalName(Object object) throws Exception {
        if (AopUtils.isJdkDynamicProxy(object)) {
            return ((Advised) object).getTargetSource().getTarget().getClass().getCanonicalName();
        } else if (AopUtils.isCglibProxy(object)) {
            return ClassUtils.getUserClass(object).getCanonicalName();
        } else {
            return object.getClass().getCanonicalName();
        }
    }

    @Override
    public Class getTargetObjectClass(Object object) throws Exception {
        final String targetObjectClassCanonicalName = getTargetObjectClassCanonicalName(object);
        return Thread.currentThread().getContextClassLoader().loadClass(targetObjectClassCanonicalName);
    }
}
