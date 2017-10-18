package eu.domibus.common.model.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Methods annotated with this annotation will be linked with a before advice {@link eu.domibus.common.aspect.BasicAuditAspect} that will
 * add some audit information if the method contains a uniquer parameter of type {@link eu.domibus.ebms3.common.model.AbstractBaseAuditEntity}.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BasicAudit {
    String name = "";
    ModificationType modificationType = ModificationType.ADD;
}
