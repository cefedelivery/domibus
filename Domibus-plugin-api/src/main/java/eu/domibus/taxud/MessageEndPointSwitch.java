package eu.domibus.taxud;

import eu.domibus.plugin.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

public class MessageEndPointSwitch {

    private final static Logger LOG = LoggerFactory.getLogger(MessageEndPointSwitch.class);

    private final static String ORIGINAL_SENDER = "originalSender";

    private final static String FINAL_RECIPIENT = "finalRecipient";

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

       /* LOG.info("switching end points from:");
        LOG.info("[{}] value: [{}] type: [{}]",ORIGINAL_SENDER,originalSender.getValue(),originalSender.getType());
        LOG.info("[{}] value: [{}] type: [{}]",FINAL_RECIPIENT,finalRecipient.getValue(),finalRecipient.getType());*/

        properties.clear();

        Submission.TypedProperty newOriginalSender = new Submission.TypedProperty(ORIGINAL_SENDER, finalRecipientValue, finalRecipientType);
        Submission.TypedProperty newFinalRecipient = new Submission.TypedProperty(FINAL_RECIPIENT, originalSenderValue, originalSenderType);

        properties.add(newOriginalSender);
        properties.add(newFinalRecipient);

       /* LOG.info("to:");
        LOG.info("[{}] value: [{}] type: [{}]",ORIGINAL_SENDER,newOriginalSender.getValue(),newOriginalSender.getType());
        LOG.info("[{}] value: [{}] type: [{}]",FINAL_RECIPIENT,newFinalRecipient.getValue(),newFinalRecipient.getType());*/
    }
}
