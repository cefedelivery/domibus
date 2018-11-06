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
    public void testTransformFromSubmissionNullConversationId(final @Mocked Submission submission) {
        new Expectations() {{
            submission.getConversationId();
            result = null;
        }};

        String conversationId = submissionAS4Transformer.transformFromSubmission(submission).getCollaborationInfo().getConversationId();
        Assert.assertTrue(conversationId == null);

    }

    @Test
    public void testTransformFromSubmissionEmptyConversationId(final @Mocked Submission submission) {
        new Expectations() {{
            submission.getConversationId();
            result = "   ";
        }};

        String conversationId = submissionAS4Transformer.transformFromSubmission(submission).getCollaborationInfo().getConversationId();
        Assert.assertTrue(StringUtils.isEmpty(conversationId));
    }

    @Test
    public void testTransformFromSubmissionNonEmptyConversationId(final @Mocked Submission submission) {
        String submittedConvId = "submittedConvId";
        new Expectations() {{
            submission.getConversationId();
            result = submittedConvId;
        }};

        String conversationId = submissionAS4Transformer.transformFromSubmission(submission).getCollaborationInfo().getConversationId();
        Assert.assertTrue(conversationId.equals(submittedConvId));
    }
}
