package eu.domibus.plugin.kerkovi;


import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.springframework.stereotype.Service;

@Service
public class KerkoviAS4Transformer implements MessageSubmissionTransformer<Submission>, MessageRetrievalTransformer<Submission> {

    @Override
    public Submission transformFromSubmission(final Submission submission, final Submission target) {
        return submission;
    }

    @Override
    public Submission transformToSubmission(final Submission submission) {
        return submission;
    }
}
