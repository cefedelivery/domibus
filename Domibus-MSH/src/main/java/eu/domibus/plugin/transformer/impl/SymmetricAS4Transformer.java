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

package eu.domibus.plugin.transformer.impl;

import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Christian Koch, Stefan Mueller
 */


@org.springframework.stereotype.Service
public class SymmetricAS4Transformer implements MessageSubmissionTransformer<Messaging>, MessageRetrievalTransformer<UserMessage> {
    @Autowired
    private SubmissionAS4Transformer submissionAS4Transformer;


    @Override
    public UserMessage transformFromSubmission(final Submission submission, final UserMessage target) {
        return this.submissionAS4Transformer.transformFromSubmission(submission);
    }

    @Override
    public Submission transformToSubmission(final Messaging messageData) {
        return this.submissionAS4Transformer.transformFromMessaging(messageData.getUserMessage());
    }
}
