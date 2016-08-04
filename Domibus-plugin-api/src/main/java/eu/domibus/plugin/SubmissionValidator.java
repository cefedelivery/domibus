package eu.domibus.plugin;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
public interface SubmissionValidator {

    void validate(Submission submission) throws SubmissionValidationException;
}
