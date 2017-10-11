package eu.domibus.common.model.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Methods annotated with this annotation will be linked with a before advice that will
 * add some audit information if the method contains parameters of type {@see AbstractBaseAuditEntity}.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BasicAudit {
}
