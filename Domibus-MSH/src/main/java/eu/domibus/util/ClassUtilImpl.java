package eu.domibus.util;

import eu.domibus.api.util.ClassUtil;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;


/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@Component
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ClassUtilImpl implements ClassUtil {

    @Override
    public String getTargetObjectClassCanonicalName(Object object)  {
        if (AopUtils.isJdkDynamicProxy(object)) {
            try {
                return ((Advised) object).getTargetSource().getTarget().getClass().getCanonicalName();
            } catch (Exception e) {
                throw new RuntimeException("Error getting the class canonical name", e);
            }
        } else if (AopUtils.isCglibProxy(object)) {
            return ClassUtils.getUserClass(object).getCanonicalName();
        } else {
            return object.getClass().getCanonicalName();
        }
    }

    @Override
    public Class getTargetObjectClass(Object object) throws ClassNotFoundException {
        final String targetObjectClassCanonicalName = getTargetObjectClassCanonicalName(object);
        return Thread.currentThread().getContextClassLoader().loadClass(targetObjectClassCanonicalName);
    }
}
