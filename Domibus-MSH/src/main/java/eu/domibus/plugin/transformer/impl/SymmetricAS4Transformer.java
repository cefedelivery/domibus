package eu.domibus.plugin.transformer.impl;

import eu.domibus.api.message.ebms3.model.Messaging;
import eu.domibus.api.message.ebms3.model.UserMessage;
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
