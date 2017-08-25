
package eu.domibus.plugin.validation;

import eu.domibus.plugin.validation.exception.ValidationException;

/**
 * Validator is an interface for all Validators inside the e-CODEX backend
 * architecture.
 *
 * @param <T>
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
public interface Validator<T> {

    /**
     * This method validates the given object. If the validation fails, an
     * excpetion is thrown.
     *
     * @param message message to validate
     * @throws ValidationException
     */
    void validate(T message);
}
