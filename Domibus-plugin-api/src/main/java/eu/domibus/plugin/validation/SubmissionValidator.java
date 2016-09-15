package eu.domibus.plugin.validation;

import eu.domibus.plugin.Submission;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
public interface SubmissionValidator {

    void validate(Submission submission) throws SubmissionValidationException;
}
