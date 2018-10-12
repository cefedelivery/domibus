package eu.domibus.plugin.fs;

import eu.domibus.plugin.Submission;
import eu.domibus.plugin.fs.ebms3.*;
import eu.domibus.plugin.fs.exception.FSPayloadException;
import eu.domibus.plugin.fs.exception.FSPluginException;
import eu.domibus.plugin.transformer.MessageRetrievalTransformer;
import eu.domibus.plugin.transformer.MessageSubmissionTransformer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for transformations from {@link FSMessage} to
 * {@link eu.domibus.plugin.Submission} and vice versa
 *
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 */
@Component
public class FSMessageTransformer
        implements MessageRetrievalTransformer<FSMessage>, MessageSubmissionTransformer<FSMessage> {

    private static final String PAYLOAD_PROPERTY_MIME_TYPE = "MimeType";

    /** we used this attribute name and not FileName to avoid name collision with Domibus core class SubmissionAS4Transformer */
    private static final String PAYLOAD_PROPERTY_FILE_NAME = "PayloadName";

    private final ObjectFactory objectFactory = new ObjectFactory();

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
        UserMessage metadata = objectFactory.createUserMessage();
        metadata.setPartyInfo(getPartyInfoFromSubmission(submission));
        metadata.setCollaborationInfo(getCollaborationInfoFromSubmission(submission));
        metadata.setMessageProperties(getMessagePropertiesFromSubmission(submission));
        Map<String, FSPayload> dataHandlers = getPayloadsFromSubmission(submission);
        return new FSMessage(dataHandlers, metadata);
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

        setPartyInfoToSubmission(submission, metadata.getPartyInfo());
        setCollaborationInfoToSubmission(submission, metadata.getCollaborationInfo());
        setMessagePropertiesToSubmission(submission, metadata.getMessageProperties());
        try {
            setPayloadToSubmission(submission, messageIn.getPayloads());
        } catch (FSPayloadException ex) {
            throw new FSPluginException("Could not set payload to Submission", ex);
        }
        return submission;
    }

    private void setPayloadToSubmission(Submission submission, final Map<String, FSPayload> dataHandlers) {
        for (Map.Entry<String, FSPayload> entry : dataHandlers.entrySet()) {
            final String contentId = entry.getKey();
            final FSPayload fsPayload = entry.getValue();
            DataHandler dataHandler = fsPayload.getDataHandler();
            final String fileName = fsPayload.getFileName();
            final String mimeType = FSMimeTypeHelper.getMimeType(dataHandler.getName());
            if (StringUtils.isEmpty(mimeType)) {
                throw new FSPayloadException("Could not detect mime type for " + dataHandler.getName());
            }
            ArrayList<Submission.TypedProperty> payloadProperties = new ArrayList<>(2);
            payloadProperties.add(new Submission.TypedProperty(PAYLOAD_PROPERTY_MIME_TYPE, mimeType));
            payloadProperties.add(new Submission.TypedProperty(PAYLOAD_PROPERTY_FILE_NAME, fileName));
            submission.addPayload(contentId, dataHandler, payloadProperties);
        }
    }

    private Map<String, FSPayload> getPayloadsFromSubmission(Submission submission) {
        Map<String, FSPayload> result = new HashMap<>(submission.getPayloads().size());
        for (final Submission.Payload payload : submission.getPayloads()) {

            //mime type
            String mimeType = extractPayloadProperty(payload, PAYLOAD_PROPERTY_MIME_TYPE);
            if (mimeType == null) {
                mimeType = payload.getPayloadDatahandler().getContentType();
            }

            //file name
            final String fileName = extractPayloadProperty(payload, PAYLOAD_PROPERTY_FILE_NAME);
            
            FSPayload fsPayload = new FSPayload(mimeType, fileName, payload.getPayloadDatahandler());
            result.put(payload.getContentId(), fsPayload);
        }
        return result;
    }

    private String extractPayloadProperty(Submission.Payload payload, String propertyName) {
        String propertyValue = null;
        for (Submission.TypedProperty payloadProperty : payload.getPayloadProperties()) {
            if (payloadProperty.getKey().equals(propertyName)) {
                propertyValue = payloadProperty.getValue();
                break;
            }
        }
        return propertyValue;
    }

    private void setMessagePropertiesToSubmission(Submission submission, MessageProperties messageProperties) {
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

    private MessageProperties getMessagePropertiesFromSubmission(Submission submission) {
        MessageProperties messageProperties = objectFactory.createMessageProperties();

        for (Submission.TypedProperty typedProperty: submission.getMessageProperties()) {
            Property messageProperty = objectFactory.createProperty();
            messageProperty.setType(typedProperty.getType());
            messageProperty.setName(typedProperty.getKey());
            messageProperty.setValue(typedProperty.getValue());
            messageProperties.getProperty().add(messageProperty);
        }
        return messageProperties;
    }

    private void setCollaborationInfoToSubmission(Submission submission, CollaborationInfo collaborationInfo) {
        AgreementRef agreementRef = collaborationInfo.getAgreementRef();
        Service service = collaborationInfo.getService();
        
        if (agreementRef != null) {
            submission.setAgreementRef(agreementRef.getValue());
            submission.setAgreementRefType(agreementRef.getType());
        }
        submission.setService(service.getValue());
        submission.setServiceType(service.getType());
        submission.setAction(collaborationInfo.getAction());
        if(collaborationInfo.getConversationId() != null) {
            submission.setConversationId(collaborationInfo.getConversationId());
        }
    }

    private CollaborationInfo getCollaborationInfoFromSubmission(Submission submission) {
        AgreementRef agreementRef = objectFactory.createAgreementRef();
        agreementRef.setType(submission.getAgreementRefType());
        agreementRef.setValue(submission.getAgreementRef());

        Service service = objectFactory.createService();
        service.setType(submission.getServiceType());
        service.setValue(submission.getService());

        CollaborationInfo collaborationInfo = objectFactory.createCollaborationInfo();
        collaborationInfo.setAgreementRef(agreementRef);
        collaborationInfo.setService(service);
        collaborationInfo.setAction(submission.getAction());
        collaborationInfo.setConversationId(submission.getConversationId());

        return collaborationInfo;
    }

    private void setPartyInfoToSubmission(Submission submission, PartyInfo partyInfo) {
        From from = partyInfo.getFrom();
        To to = partyInfo.getTo();
        
        submission.addFromParty(from.getPartyId().getValue(), from.getPartyId().getType());
        submission.setFromRole(from.getRole());
        if (to != null) {
            submission.addToParty(to.getPartyId().getValue(), to.getPartyId().getType());
            submission.setToRole(to.getRole());
        }
    }

    private PartyInfo getPartyInfoFromSubmission(Submission submission) {
        // From
        Submission.Party fromParty = submission.getFromParties().iterator().next();
        String fromRole = submission.getFromRole();

        PartyId fromPartyId = objectFactory.createPartyId();
        fromPartyId.setType(fromParty.getPartyIdType());
        fromPartyId.setValue(fromParty.getPartyId());

        From from = objectFactory.createFrom();
        from.setPartyId(fromPartyId);
        from.setRole(fromRole);

        // To
        Submission.Party toParty = submission.getToParties().iterator().next();
        String toRole = submission.getToRole();

        PartyId toPartyId = objectFactory.createPartyId();
        toPartyId.setType(toParty.getPartyIdType());
        toPartyId.setValue(toParty.getPartyId());

        To to = objectFactory.createTo();
        to.setPartyId(toPartyId);
        to.setRole(toRole);

        // PartyInfo
        PartyInfo partyInfo = objectFactory.createPartyInfo();
        partyInfo.setFrom(from);
        partyInfo.setTo(to);

        return partyInfo;
    }
}
