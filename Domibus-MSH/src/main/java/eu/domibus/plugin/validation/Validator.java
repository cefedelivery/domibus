/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

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
