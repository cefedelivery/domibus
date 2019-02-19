package eu.domibus.plugin.transformer.impl;

import eu.domibus.common.AutoCloseFileDataSource;
import eu.domibus.core.message.fragment.MessageFragmentEntity;
import eu.domibus.core.message.fragment.MessageGroupEntity;
import eu.domibus.ebms3.common.model.*;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class UserMessageDefaultFactory implements UserMessageFactory {

    private static final List<String> ALLOWED_PROPERTIES = Arrays.asList(new String[]{"originalSender", "finalRecipient", "trackingIdentifier"});


    @Override
    public UserMessage createUserMessageFragment(UserMessage sourceMessage, MessageGroupEntity messageGroupEntity, Long fragmentNumber, String fragmentFile) {
        UserMessage result = new UserMessage();
        result.setSplitAndJoin(true);
        String messageId = sourceMessage.getMessageInfo().getMessageId() + "_" + fragmentNumber;
        result.setCollaborationInfo(createCollaborationInfo(sourceMessage.getCollaborationInfo()));
        result.setMessageInfo(createMessageInfo(sourceMessage.getMessageInfo(), messageId));
        result.setPartyInfo(createPartyInfo(sourceMessage.getPartyInfo()));
        result.setMessageProperties(createMessageProperties(sourceMessage.getMessageProperties()));
        result.setPayloadInfo(createPayloadInfo(fragmentFile, fragmentNumber));

        MessageFragmentEntity messageFragmentEntity = createMessageFragmentEntity(messageGroupEntity, fragmentNumber);
        result.setMessageFragment(messageFragmentEntity);

        return result;
    }

    @Override
    public UserMessage cloneUserMessageFragment(UserMessage userMessageFragment) {
        UserMessage result = new UserMessage();
        result.setCollaborationInfo(createCollaborationInfo(userMessageFragment.getCollaborationInfo()));
        result.setMessageInfo(createMessageInfo(userMessageFragment.getMessageInfo(), userMessageFragment.getMessageInfo().getMessageId()));
        result.setPartyInfo(createPartyInfo(userMessageFragment.getPartyInfo()));
        result.setMessageProperties(createMessageProperties(userMessageFragment.getMessageProperties()));
        return result;
    }

    protected MessageFragmentEntity createMessageFragmentEntity(MessageGroupEntity messageGroupEntity, Long fragmentNumber) {
        MessageFragmentEntity result = new MessageFragmentEntity();
        result.setFragmentNumber(fragmentNumber);
        result.setGroupId(messageGroupEntity.getGroupId());
        return result;
    }

    protected PayloadInfo createPayloadInfo(String fragmentFile, Long fragmentNumber) {
        final PayloadInfo payloadInfo = new PayloadInfo();

        final PartInfo partInfo = new PartInfo();
        partInfo.setInBody(false);
        partInfo.setPayloadDatahandler(new DataHandler(new AutoCloseFileDataSource(fragmentFile)));
        partInfo.setHref("cid:fragment" + fragmentNumber);
        partInfo.setFileName(fragmentFile);
        partInfo.setLength(new File(fragmentFile).length());
        final PartProperties partProperties = new PartProperties();
        final Property property = new Property();
        property.setName(Property.MIME_TYPE);
        property.setValue("application/octet-stream");
        partProperties.getProperties().add(property);

        partInfo.setPartProperties(partProperties);
        payloadInfo.getPartInfo().add(partInfo);
        return payloadInfo;
    }

    protected CollaborationInfo createCollaborationInfo(final CollaborationInfo source) {
        final CollaborationInfo collaborationInfo = new CollaborationInfo();

        collaborationInfo.setConversationId(source.getConversationId());
        collaborationInfo.setAction(source.getAction());
        if (source.getAgreementRef() != null) {
            final AgreementRef agreementRef = new AgreementRef();
            agreementRef.setValue(source.getAgreementRef().getValue());
            agreementRef.setType(source.getAgreementRef().getType());
            collaborationInfo.setAgreementRef(agreementRef);
        }

        final eu.domibus.ebms3.common.model.Service service = new eu.domibus.ebms3.common.model.Service();
        service.setValue(source.getService().getValue());
        service.setType(source.getService().getType());
        collaborationInfo.setService(service);

        return collaborationInfo;
    }

    protected MessageInfo createMessageInfo(final MessageInfo source, String messageId) {
        final MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId(messageId);
        messageInfo.setTimestamp(source.getTimestamp());
        messageInfo.setRefToMessageId(source.getRefToMessageId());
        return messageInfo;
    }

    protected PartyInfo createPartyInfo(final PartyInfo source) {
        final PartyInfo partyInfo = new PartyInfo();

        final From from = new From();
        if (source.getFrom() != null) {
            from.setRole(source.getFrom().getRole());
            for (final PartyId party : source.getFrom().getPartyId()) {
                final PartyId newParty = new PartyId();
                newParty.setValue(party.getValue());
                newParty.setType(party.getType());
                from.getPartyId().add(newParty);
            }
        }
        partyInfo.setFrom(from);

        final To to = new To();
        if (source.getTo() != null) {
            to.setRole(source.getTo().getRole());
            for (final PartyId party : source.getTo().getPartyId()) {
                final PartyId newParty = new PartyId();
                newParty.setValue(party.getValue());
                newParty.setType(party.getType());
                to.getPartyId().add(newParty);
            }
        }
        partyInfo.setTo(to);

        return partyInfo;
    }

    protected MessageProperties createMessageProperties(final MessageProperties source) {
        final MessageProperties messageProperties = new MessageProperties();

        for (Property sourceProperty : source.getProperty()) {
            if (ALLOWED_PROPERTIES.contains(sourceProperty.getName())) {
                final Property prop = new Property();
                prop.setName(sourceProperty.getName());
                prop.setValue(sourceProperty.getValue());
                prop.setType(sourceProperty.getType());
                messageProperties.getProperty().add(prop);
            }
        }

        return messageProperties;
    }
}
