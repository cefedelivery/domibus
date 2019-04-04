package eu.domibus.web.rest.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = BlacklistValidator.class)
@Documented
public @interface NotBlacklisted {

    String message() default "Blacklisted character detected.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
