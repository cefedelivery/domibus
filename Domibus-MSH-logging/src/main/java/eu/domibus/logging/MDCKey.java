package eu.domibus.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a method that is putting one key or multiple in MDC
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MDCKey {

     String[] value();
}