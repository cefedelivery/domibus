package eu.domibus.submission.validation;

import eu.domibus.plugin.Submission;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.plugin.validation.SubmissionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
@Component("onePayloadSubmissionValidator")
public class OnePayloadSubmissionValidator implements SubmissionValidator {

    @Autowired
    @Qualifier("payloadsRequiredSubmissionValidator")
    SubmissionValidator payloadsRequiredValidator;

    @Override
    public void validate(Submission submission) throws SubmissionValidationException {
        payloadsRequiredValidator.validate(submission);
        if (submission.getPayloads().size() != 1) {
            throw new SubmissionValidationException("Only one payload is required");
        }
    }
}
