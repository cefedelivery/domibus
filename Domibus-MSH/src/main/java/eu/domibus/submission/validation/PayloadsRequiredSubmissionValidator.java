package eu.domibus.submission.validation;

import eu.domibus.plugin.Submission;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.plugin.validation.SubmissionValidator;
import org.springframework.stereotype.Component;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
@Component("payloadsRequiredSubmissionValidator")
public class PayloadsRequiredSubmissionValidator implements SubmissionValidator {

    @Override
    public void validate(Submission submission) throws SubmissionValidationException {
        if (submission.getPayloads() == null || submission.getPayloads().isEmpty()) {
            throw new SubmissionValidationException("No payloads found. At least one payload is required");
        }
    }
}
