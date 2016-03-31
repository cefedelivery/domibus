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

package eu.domibus.plugin;

import eu.domibus.messaging.MessageNotFoundException;

import java.util.Collection;

/**
 * Provides access to messages waiting to be pulled by plugins using BackendConnector.Mode.PULL.
 *
 * @author Christian Koch, Stefan Mueller
 */
public interface MessageLister {

    /**
     * Lists all messages pending for download by the backend
     *
     * @return a collection of messageIds pending download
     */
    Collection<String> listPendingMessages();

    /**
     * removes the message with the corresponding id from the download queue
     *
     * @param messageId id of the message to be removed
     * @throws MessageNotFoundException if the message is not pending
     */
    void removeFromPending(String messageId) throws MessageNotFoundException;
}
