
package eu.domibus.plugin.validation;

import eu.domibus.plugin.validation.exception.ValidationException;

/**
 * Validator is an interface for all Validators inside the e-CODEX backend
 * architecture.
 *
 * @param <T> the message type
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
public interface Validator<T> {

    /**
     * This method validates the given object. If the validation fails, an
     * exception is thrown.
     *
     * @param message message to validate
     */
    void validate(T message);
}
