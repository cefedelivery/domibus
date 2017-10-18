package eu.domibus.common.model.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * This annotation allows to group modifications from different entities under the same locical name.
 * For instance changing the identifier of a Party would appear under "Party" logical change also the class changed is
 * is not Party itself. It also allows to scan in a generic way the entities that we are auditing.
 * EG: used to diplay the different types of audit in the admin console.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RevisionLogicalName {

    String value() default "";

    int auditOrder() default 0;

}
