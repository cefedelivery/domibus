package eu.domibus.controller;

import eu.domibus.plugin.JsonSubmission;
import eu.domibus.plugin.Submission;
import eu.domibus.taxud.SubmissionLogging;
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

    public final static String ORIGINAL_SENDER = "originalSender";

    public final static String FINAL_RECIPIENT = "finalRecipient";

    private SubmissionLogging submissionLogging;
    public EndPointHelper() {
        submissionLogging =new SubmissionLogging();
    }

    public void switchEndPoint(JsonSubmission submission) {
        Collection<JsonSubmission.TypedProperty> properties = submission.getMessageProperties();
        JsonSubmission.TypedProperty originalSender=null;
        JsonSubmission.TypedProperty finalRecipient=null;
        for (JsonSubmission.TypedProperty property : properties) {
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

        properties.clear();

        JsonSubmission.TypedProperty newOriginalSender = new JsonSubmission.TypedProperty(ORIGINAL_SENDER, finalRecipientValue, finalRecipientType);
        JsonSubmission.TypedProperty newFinalRecipient = new JsonSubmission.TypedProperty(FINAL_RECIPIENT, originalSenderValue, originalSenderType);

        properties.add(newOriginalSender);
        properties.add(newFinalRecipient);

    }
}
