package eu.domibus.submission;

import eu.domibus.plugin.SubmissionValidator;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
public interface SubmissionValidatorProvider {

    SubmissionValidator getSubmissionValidator(String backendName);
}
