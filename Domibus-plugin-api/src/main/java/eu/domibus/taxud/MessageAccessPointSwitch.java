package eu.domibus.taxud;

import eu.domibus.plugin.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

public class MessageAccessPointSwitch {
    private final static Logger LOG = LoggerFactory.getLogger(MessageAccessPointSwitch.class);

    private SubmissionLog submissionLog;

    public MessageAccessPointSwitch() {
        this.submissionLog = new SubmissionLog();
    }

    public  void switchAccessPoint(Submission submission) {
        Set<Submission.Party> fromParties = new HashSet<>(submission.getFromParties());
        Set<Submission.Party> toParties = new HashSet<>(submission.getToParties());

       LOG.info("switching access point from :");
        submissionLog.logAccesPoints(submission);

        submission.getFromParties().clear();
        submission.getToParties().clear();

        submission.getFromParties().addAll(toParties);
        submission.getToParties().addAll(fromParties);

        LOG.info("to:");
        submissionLog.logAccesPoints(submission);

        String fromRole = submission.getFromRole();
        String toRole = submission.getToRole();

        submission.setFromRole(toRole);
        submission.setToRole(fromRole);
    }
}
