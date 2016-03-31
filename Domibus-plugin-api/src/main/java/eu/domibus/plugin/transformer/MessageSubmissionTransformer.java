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

package eu.domibus.plugin.transformer;

import eu.domibus.plugin.Submission;

/**
 * Implementations of this interface transform a message of type {@literal <T>}
 * to an object of type {@link eu.domibus.plugin.Submission}
 *
 * @param <T> Data transfer object
 *            (http://en.wikipedia.org/wiki/Data_transfer_object) transported between the
 *            backend and holodeck
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

public interface MessageSubmissionTransformer<T> {

    /**
     * transforms the typed object to an EbMessage
     *
     * @param messageData the message to be transformed
     * @return the transformed message
     */
    Submission transformToSubmission(T messageData);
}
