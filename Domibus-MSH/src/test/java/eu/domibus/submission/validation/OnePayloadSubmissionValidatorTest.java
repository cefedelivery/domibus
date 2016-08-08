package eu.domibus.submission.validation;

import eu.domibus.plugin.Submission;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.plugin.validation.SubmissionValidator;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by baciuco on 08/08/2016.
 */
@RunWith(JMockit.class)
public class OnePayloadSubmissionValidatorTest {

    @Injectable
    SubmissionValidator payloadsRequiredSubmissionValidator;

    @Tested
    OnePayloadSubmissionValidator onePayloadSubmissionValidator;

    @Test
    public void testValidateWithOnePayload(@Injectable final Submission submission,
                                         @Injectable final Submission.Payload payload1) throws Exception {
        new Expectations() {{
            submission.getPayloads();
            Set<Submission.Payload> payloads = new HashSet<>();
            payloads.add(payload1);
            result = payloads;
        }};
        onePayloadSubmissionValidator.validate(submission);
    }

    @Test(expected = SubmissionValidationException.class)
    public void testValidateWithNoPayloads(@Injectable final Submission submission,
                                           @Injectable final Submission.Payload payload1,
                                           @Injectable final Submission.Payload payload2) throws Exception {
        new Expectations() {{
            submission.getPayloads();
            Set<Submission.Payload> payloads = new HashSet<>();
            payloads.add(payload1);
            payloads.add(payload2);
            result = payloads;
        }};
        onePayloadSubmissionValidator.validate(submission);
    }

}
