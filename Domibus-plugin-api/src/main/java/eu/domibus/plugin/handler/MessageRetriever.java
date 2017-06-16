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

import eu.domibus.common.ErrorResult;
import eu.domibus.common.MessageStatus;
import eu.domibus.messaging.MessageNotFoundException;

import java.util.List;

/**
 * Implementations of this interface handle the retrieval of messages from
 * Domibus to the backend.
 *
 * @deprecated generic type <T> is deprecated and will be replaced by <Submission> in Release 3.3
 *
 * @param <T> Data transfer object
 *            (http://en.wikipedia.org/wiki/Data_transfer_object) transported between the
 *            backend and Domibus
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
@Deprecated
public interface MessageRetriever<T> {

    /**
     * provides the message with the corresponding messageId
     *
     * @deprecated generic type <T> is deprecated and will be replaced by <Submission> in Release 3.3
     *
     * @param messageId the messageId of the message to retrieve
     * @return the message object with the given messageId
     */
    @Deprecated
    T downloadMessage(String messageId) throws MessageNotFoundException;

    /**
     * Returns message status {@link eu.domibus.common.MessageStatus} for message with messageid
     *
     * @param messageId id of the message the status is requested for
     * @return the message status {@link eu.domibus.common.MessageStatus}
     */
    //@todo
    MessageStatus getMessageStatus(String messageId);

    /**
     * Returns List {@link java.util.List} of error logs {@link ErrorResult} for message with messageid
     *
     * @param messageId id of the message the errors are requested for
     * @return the list of error log entries {@link java.util.List< ErrorResult >}
     */
    List<? extends ErrorResult> getErrorsForMessage(String messageId);
}
