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

import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import eu.domibus.plugin.exception.exception.TransformationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;

import java.util.concurrent.Future;

public class ExampleBackendConnector extends AbstractBackendConnector<MyObjectIn, MyObjectOut>
                                     implements BackendConnector<MyObjectIn, MyObjectOut>{
                                    //The implements clause is important as Spring got some issues with abstract classes

    private MessageSubmissionTransformer<MyObjectIn> messageSubmissionTransformer;
    private MessageRetrievalTransformer<MyObjectOut> messageRetrievalTransformer;

    @Override
    public MessageSubmissionTransformer<MyObjectIn> getMessageSubmissionTransformer() {
        return this.getMessageSubmissionTransformer();
    }

    public void setMessageSubmissionTransformer(MessageSubmissionTransformer<MyObjectIn> messageSubmissionTransformer) {
        this.messageSubmissionTransformer = messageSubmissionTransformer;
    }

    @Override
    public MessageRetrievalTransformer<MyObjectOut> getMessageRetrievalTransformer() {
        return this.getMessageRetrievalTransformer();
    }

    public void setMessageRetrievalTransformer(MessageRetrievalTransformer<MyObjectOut> messageRetrievalTransformer) {
        this.messageRetrievalTransformer = messageRetrievalTransformer;
    }

    @Override
    public boolean isResponsible(MessageMetadata metadata) {
        /*
        This is the most simple case. If you have only one active
        plugin it automatically is responsible for all messages.
        */
        return true;
    }

    @Override
    @Async
    public Future<Boolean> deliverMessage(final MessageMetadata metadata) {
        //example of how to handle async calls with spring
        return new AsyncResult<>(this.submitToBackend(metadata.getMessageId()));
    }

    private boolean submitToBackend(String messageId) {
        /*
        Here you could put the message on a queue, call an external webservice or store it on the file system.
         */

        throw new UnsupportedOperationException("Needs to be implemented");
    }

    /*
    This is an example of the method that is called from outside. In a JMS implementation it would most likely be
    called onMessage(Message message), while in a webservice plugin it would have the name of the webservice operation.
    */
    public String handleMessageFromBackend(MyObjectIn messageFromBackend) throws ValidationException, TransformationException {
        /*
         Handle stuff specific to your implementation, i.e. get message from JMS or webservice, check security etc.
         */
        return submit(messageFromBackend);
    }

}
