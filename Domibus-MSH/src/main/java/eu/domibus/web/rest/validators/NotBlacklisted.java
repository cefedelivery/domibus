package eu.domibus.web.rest.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for custom validator that checks that the value does not contain any char from the blacklist
 *
 * @author Ion Perpegel
 * since 4.1
 */
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = BlacklistValidator.class)
@Documented
public @interface NotBlacklisted {

    String message() default "Blacklisted character detected.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
