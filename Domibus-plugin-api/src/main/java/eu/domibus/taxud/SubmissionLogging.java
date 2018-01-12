package eu.domibus.taxud;

import eu.domibus.plugin.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

public class SubmissionLogging {

    private final static Logger LOG = LoggerFactory.getLogger(SubmissionLogging.class);

    public final static String ORIGINAL_SENDER = "originalSender";

    public final static String FINAL_RECIPIENT = "finalRecipient";

    public void logAccesPoints(Submission submission){
        for (Submission.Party party : submission.getFromParties()) {
            LOG.debug("From [{}] with type [{}]",party.getPartyId(),party.getPartyIdType());
        }
        for (Submission.Party party : submission.getToParties()) {
            LOG.debug("to [{}] with type [{}]",party.getPartyId(),party.getPartyIdType());
        }
    }

    public void logEndPoints(Submission.TypedProperty originalSender,Submission.TypedProperty finalRecipient){
        LOG.debug("[{}] value: [{}] type: [{}]",ORIGINAL_SENDER,originalSender.getValue(),originalSender.getType());
        LOG.debug("[{}] value: [{}] type: [{}]",FINAL_RECIPIENT,finalRecipient.getValue(),finalRecipient.getType());
    }
}
