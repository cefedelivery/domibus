package eu.domibus.plugin.transformer;

import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.impl.SubmissionAS4Transformer;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * // TODO reach 70% coverage.
 *
 * @author Ion Perpegel
 * @since 4.1
 */
@RunWith(JMockit.class)
public class SubmissionAS4TransformerTest {

    @Injectable
    private MessageIdGenerator messageIdGenerator;

    @Tested
    SubmissionAS4Transformer submissionAS4Transformer;

    @Test
    public void testTransformFromSubmission(final @Mocked Submission submission) {
        String submittedConvId = "submittedConvId";
        String generatedConvId = "guid";

        new Expectations() {{
            messageIdGenerator.generateMessageId();
            result = generatedConvId;

            submission.getConversationId();
            result = null;
            result = StringUtils.EMPTY;
            result = submittedConvId;
        }};

        String conversationId = submissionAS4Transformer.transformFromSubmission(submission).getCollaborationInfo().getConversationId();
        Assert.assertEquals(generatedConvId, conversationId);

        conversationId = submissionAS4Transformer.transformFromSubmission(submission).getCollaborationInfo().getConversationId();
        Assert.assertEquals(StringUtils.EMPTY, conversationId);

        conversationId = submissionAS4Transformer.transformFromSubmission(submission).getCollaborationInfo().getConversationId();
        Assert.assertEquals(submittedConvId, conversationId);
    }

}
