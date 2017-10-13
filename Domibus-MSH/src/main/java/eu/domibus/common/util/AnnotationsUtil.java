package eu.domibus.common.util;

import eu.domibus.common.model.common.RevisionLogicalName;
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
     * Utility method that verify if an annotation is present on the class and returns the default annotation value
     * if it is the case.
     *
     * @param clazz      a class.
     * @param annotation an annotation.
     * @return an optional value.
     */
    public Optional<String> getDefaultValue(final Class clazz, final Class annotation) {
        if (clazz.isAnnotationPresent(RevisionLogicalName.class)) {
            String logicalName = (String) AnnotationUtils.getDefaultValue(clazz.getAnnotation(annotation));
            return Optional.of(logicalName);
        }
        return Optional.empty();
    }

    /**
     * Utility method that verify if an annotation is present on the class and returns the annotation value
     * if it is the case.
     *
     * @param clazz      a class.
     * @param annotation an annotation.
     * @return an optional value.
     */
    public Optional<String> getValue(final Class clazz, final Class annotation) {
        if (clazz.isAnnotationPresent(RevisionLogicalName.class)) {
            String logicalName = (String) AnnotationUtils.getValue(clazz.getAnnotation(annotation));
            return Optional.of(logicalName);
        }
        return Optional.empty();
    }
}

