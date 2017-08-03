package eu.domibus.plugin.fs;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.util.FileObjectDataSource;
import org.springframework.stereotype.Component;

import eu.domibus.plugin.fs.ebms3.AgreementRef;
import eu.domibus.plugin.fs.ebms3.CollaborationInfo;
import eu.domibus.plugin.fs.ebms3.From;
import eu.domibus.plugin.fs.ebms3.MessageProperties;
import eu.domibus.plugin.fs.ebms3.PartyInfo;
import eu.domibus.plugin.fs.ebms3.Property;
import eu.domibus.plugin.fs.ebms3.Service;
import eu.domibus.plugin.fs.ebms3.To;
import eu.domibus.plugin.fs.ebms3.UserMessage;

/**
 * This class is responsible for transformations from {@link FSMessage} to
 * {@link eu.domibus.plugin.Submission} and vice versa
 *
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 */
@Component
public class FSMessageTransformer
        implements MessageRetrievalTransformer<FSMessage>, MessageSubmissionTransformer<FSMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSMessageTransformer.class);
    
    private static final String DEFAULT_CONTENT_ID = "cid:message";
    private static final String DEFAULT_MIME_TYPE =  "text/xml";
    public static final String MIME_TYPE = "MimeType";
    public static final String CHARSET = "CharacterSet";

    /**
     * The default properties to be used
     */
    private final Properties properties;
    
    /**
     * Creates a new <code>FSMessageTransformer</code>.
     */
    public FSMessageTransformer() {
        properties = new Properties();
    }
    
    /**
     * Creates a new <code>FSMessageTransformer</code> with the given properties.
     * 
     * @param defaultProperties Default properties
     * @throws java.io.IOException
     */
    public FSMessageTransformer(String defaultProperties) throws IOException {
        properties = new Properties();
        properties.load(new FileReader(defaultProperties));
    }

    /**
     * Transforms {@link eu.domibus.plugin.Submission} to {@link FSMessage}
     *
     * @param submission the message to be transformed
     * @param messageOut output target
     *
     * @return result of the transformation as {@link FSMessage}
     */
    @Override
    public FSMessage transformFromSubmission(final Submission submission, final FSMessage messageOut) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Transforms {@link FSMessage} to {@link eu.domibus.plugin.Submission}
     *
     * @param messageIn the message ({@link FSMessage}) to be tranformed
     * @return the result of the transformation as
     * {@link eu.domibus.plugin.Submission}
     */
    @Override
    public Submission transformToSubmission(final FSMessage messageIn) {
        UserMessage metadata = messageIn.getMetadata();
        Submission submission = new Submission();
        
        setPartyInfo(submission, metadata.getPartyInfo());
        
        setCollaborationInfo(submission, metadata.getCollaborationInfo());
        
        setMessageProperties(submission, metadata.getMessageProperties());
        
        setPayload(submission, messageIn.getFile());
        
        return submission;
    }

    private void setPayload(Submission submission, final FileObject file) {
        FileObjectDataSource dataSource = new FileObjectDataSource(file);
        
        ArrayList<Submission.TypedProperty> payloadProperties = new ArrayList<>(1);
        payloadProperties.add(new Submission.TypedProperty(MIME_TYPE, DEFAULT_MIME_TYPE));
        
        submission.addPayload(DEFAULT_CONTENT_ID, new DataHandler(dataSource), payloadProperties);
    }

    private void setMessageProperties(Submission submission, MessageProperties messageProperties) {
        for (Property messageProperty : messageProperties.getProperty()) {
            String name = messageProperty.getName();
            String value = messageProperty.getValue();
            String type = messageProperty.getType();
            
            if (type != null) {
                submission.addMessageProperty(name, value, type);
            } else {
                submission.addMessageProperty(name, value);
            }
        }
    }

    private void setCollaborationInfo(Submission submission, CollaborationInfo collaborationInfo) {
        AgreementRef agreementRef = collaborationInfo.getAgreementRef();
        Service service = collaborationInfo.getService();
        
        if (agreementRef != null) {
            submission.setAgreementRef(agreementRef.getValue());
            submission.setAgreementRefType(agreementRef.getType());
        }
        submission.setService(service.getValue());
        submission.setServiceType(service.getType());
        submission.setAction(collaborationInfo.getAction());
        
        // TODO: is this bit needed?
        if (collaborationInfo.getConversationId() != null) {
            submission.setConversationId(collaborationInfo.getConversationId());
        }
    }

    private void setPartyInfo(Submission submission, PartyInfo partyInfo) {
        From from = partyInfo.getFrom();
        To to = partyInfo.getTo();
        
        submission.addFromParty(from.getPartyId().getValue(), from.getPartyId().getType());
        submission.setFromRole(from.getRole());
        if (to != null) {
            submission.addToParty(to.getPartyId().getValue(), to.getPartyId().getType());
            submission.setToRole(to.getRole());
        }
    }
}
