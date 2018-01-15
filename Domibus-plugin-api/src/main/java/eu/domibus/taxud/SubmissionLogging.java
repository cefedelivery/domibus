package eu.domibus.taxud;

import eu.domibus.plugin.Submission;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

public class SubmissionLogging {

   // private final static Logger LOG = LoggerFactory.getLogger(SubmissionLogging.class);



    public void logAccesPoints(Submission submission){
        /*for (Submission.Party party : submission.getFromParties()) {
            LOG.info("From [{}] with type [{}]",party.getPartyId(),party.getPartyIdType());
        }
        for (Submission.Party party : submission.getToParties()) {
            LOG.info("to [{}] with type [{}]",party.getPartyId(),party.getPartyIdType());
        }*/
    }

    public void logEndPoints(Submission.TypedProperty originalSender,Submission.TypedProperty finalRecipient){
     //   LOG.info("[{}] value: [{}] type: [{}]",ORIGINAL_SENDER,originalSender.getValue(),originalSender.getType());
       // LOG.info("[{}] value: [{}] type: [{}]",FINAL_RECIPIENT,finalRecipient.getValue(),finalRecipient.getType());
    }


}
