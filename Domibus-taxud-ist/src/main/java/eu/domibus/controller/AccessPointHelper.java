package eu.domibus.controller;


import eu.domibus.plugin.Submission;
import eu.domibus.taxud.JsonSubmission;
import eu.domibus.taxud.SubmissionLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@Component
public class AccessPointHelper {
    private final static Logger LOG = LoggerFactory.getLogger(AccessPointHelper.class);

    private SubmissionLogging submissionLogging;

    public AccessPointHelper() {
        this.submissionLogging = new SubmissionLogging();
    }

    public  void switchAccessPoint(JsonSubmission submission) {
        Set<JsonSubmission.Party> fromParties = new HashSet<>(submission.getFromParties());
        Set<JsonSubmission.Party> toParties = new HashSet<>(submission.getToParties());

        submission.getFromParties().clear();
        submission.getToParties().clear();

        submission.getFromParties().addAll(toParties);
        submission.getToParties().addAll(fromParties);

        String fromRole = submission.getFromRole();
        String toRole = submission.getToRole();

        submission.setFromRole(toRole);
        submission.setToRole(fromRole);
    }

    public Submission.Party extractSendingAccessPoint(Submission submission){
        if(submission.getFromParties().size()>0){
            return submission.getFromParties().iterator().next();
        }
        return null;
    }
}
