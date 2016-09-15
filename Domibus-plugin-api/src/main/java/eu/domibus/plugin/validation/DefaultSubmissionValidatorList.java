package eu.domibus.plugin.validation;

import java.util.List;

/**
 * Created by Cosmin Baciu on 05-Aug-16.
 */
public class DefaultSubmissionValidatorList implements SubmissionValidatorList {

    protected List<SubmissionValidator> submissionValidators;

    @Override
    public List<SubmissionValidator> getSubmissionValidators() {
        return submissionValidators;
    }

    public void setSubmissionValidators(List<SubmissionValidator> submissionValidators) {
        this.submissionValidators = submissionValidators;
    }
}

