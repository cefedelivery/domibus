package eu.domibus.common.metrics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Thomas Dussart
 * @since 4.1
 *
 * Metric annotation to add a timer. The current implementation is using dropwizard
 * timer.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Timer {
    /**
     * @return the timer name.
     */
    String value();

    /**
     * @return the timer class.
     */
    Class<?> clazz() default Default.class;
}

