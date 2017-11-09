package eu.domibus.common.util;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Utility class to deal with annotations.
 *
 * @author Thomas Dussart
 * @since 4.0
 */
@Component
public class AnnotationsUtil {

    /**
     * Utility method that verify if an annotation is present on the class and returns the annotation value
     * if it is the case.
     *
     * @param clazz      a class.
     * @param annotation an annotation.
     * @return an optional value.
     */
    public Optional<String> getValue(final Class clazz, final Class annotation) {
        return getValue(clazz, annotation, "value", String.class);
    }

    public <E> Optional<E> getValue(final Class clazz, final Class annotation, final String methodName, Class<E> e) {
        if (clazz.isAnnotationPresent(annotation)) {
            E logicalName = (E) AnnotationUtils.getValue(clazz.getAnnotation(annotation), methodName);
            return Optional.of(logicalName);
        }
        return Optional.empty();
    }
}

