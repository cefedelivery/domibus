package eu.domibus.plugin.jms;

import eu.domibus.plugin.Submission;
import eu.domibus.taxud.SubmissionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@Component
public class EndPointHelper {

    private final static Logger LOG = LoggerFactory.getLogger(EndPointHelper.class);

    private final static String ORIGINAL_SENDER = "originalSender";

    private final static String FINAL_RECIPIENT = "finalRecipient";

    private SubmissionLog submissionLog;
    public EndPointHelper() {
        submissionLog=new SubmissionLog();
    }

    public void switchEndPoint(Submission submission) {
        Collection<Submission.TypedProperty> properties = submission.getMessageProperties();
        Submission.TypedProperty originalSender=null;
        Submission.TypedProperty finalRecipient=null;
        for (Submission.TypedProperty property : properties) {
            if(ORIGINAL_SENDER.equals(property.getKey())){
                originalSender=property;
            }
            if(FINAL_RECIPIENT.equals(property.getKey())){
                finalRecipient=property;
            }
        }

        if(originalSender==null || finalRecipient==null) return;

        String originalSenderType = originalSender.getType();
        String originalSenderValue = originalSender.getValue();

        String finalRecipientType = finalRecipient.getType();
        String finalRecipientValue = finalRecipient.getValue();

        LOG.info("switching end points from:");
        submissionLog.logEndPoints(originalSender,finalRecipient);

        properties.clear();

        Submission.TypedProperty newOriginalSender = new Submission.TypedProperty(ORIGINAL_SENDER, finalRecipientValue, finalRecipientType);
        Submission.TypedProperty newFinalRecipient = new Submission.TypedProperty(FINAL_RECIPIENT, originalSenderValue, originalSenderType);

        properties.add(newOriginalSender);
        properties.add(newFinalRecipient);

        LOG.info("to:");
        submissionLog.logEndPoints(newOriginalSender,newFinalRecipient);
    }
}
