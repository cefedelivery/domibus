package eu.domibus.plugin.jms;

import eu.domibus.plugin.Submission;
import eu.domibus.taxud.SubmissionLog;
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

    private SubmissionLog submissionLog;

    public AccessPointHelper() {
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

    public Submission.Party extractSendingAccessPoint(Submission submission){
        if(submission.getFromParties().size()>0){
            return submission.getFromParties().iterator().next();
        }
        return null;
    }
}
