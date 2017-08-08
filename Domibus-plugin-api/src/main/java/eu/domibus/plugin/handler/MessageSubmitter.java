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

package eu.domibus.plugin.handler;


import eu.domibus.messaging.MessagingProcessingException;

/**
 * Implementations of this interface handle the plugin of messages from the
 * backend to holodeck.
 *
 * @deprecated generic type <T> is deprecated and will be replaced by <Submission> in Release 4.0
 *
 * @param <T> Data transfer object
 *            (http://en.wikipedia.org/wiki/Data_transfer_object) transported between the
 *            backend and Domibus
 * @author Christian Koch, Stefan Mueller
 */
@Deprecated
public interface MessageSubmitter<T> {

    /**
     * Submits a message to Domibus to be processed.
     *
     * @param messageData the message to be processed
     * @return the messageId of the submitted message
     *
     * @deprecated generic type <T> is deprecated and will be replaced by <Submission> in Release 4.0
     */
    @Deprecated
    public String submit(T messageData, String submitterName) throws MessagingProcessingException;
}
