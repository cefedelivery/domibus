package eu.domibus.plugin.transformer.impl;

import eu.domibus.common.services.impl.MessageIdGenerator;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.plugin.Submission;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.*;

/**
 * @author Ion Perpegel, Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class SubmissionAS4TransformerTest {

    @Injectable
    private MessageIdGenerator messageIdGenerator;

    @Tested
    private SubmissionAS4Transformer submissionAS4Transformer;

    private ObjectFactory objectFactory = new ObjectFactory();

    @Test
    public void testTransformFromSubmission(final @Mocked Submission submission) {
        String submittedConvId = "submittedConvId";
        String generatedConvId = "guid";

        new Expectations() {{
            messageIdGenerator.generateMessageId();
            result = generatedConvId;

            submission.getConversationId();
            result = null;
            result = "   ";
            result = submittedConvId;
        }};

        String conversationId = submissionAS4Transformer.transformFromSubmission(submission).getCollaborationInfo().getConversationId();
        Assert.assertEquals(generatedConvId, conversationId);

        conversationId = submissionAS4Transformer.transformFromSubmission(submission).getCollaborationInfo().getConversationId();
        Assert.assertEquals("", conversationId);

        conversationId = submissionAS4Transformer.transformFromSubmission(submission).getCollaborationInfo().getConversationId();
        Assert.assertEquals(submittedConvId, conversationId);
    }

    @Test
    public void testTransformFromMessaging_NotNullUserMessage_TransformationOK(final @Mocked UserMessage userMessage,
                                                                               final @Mocked CollaborationInfo collaborationInfo) {

        final String action = "TC1Leg1";
        final String service = "bdx:noprocess";
        final String serviceType = "tc1";
        final String fromRole = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
        final String toRole = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";


        final PartInfo partInfo = objectFactory.createPartInfo();
        final String fileNameWithoutPath = createFileName();
        partInfo.setFileName(createFileNameWithFullPath(fileNameWithoutPath));
        final List<PartInfo> partInfoList = Collections.singletonList(partInfo);

        final Set<PartyId> fromPartyIdSet = createPartyId(objectFactory, "domibus-blue");

        final Set<PartyId> toPartyIdSet = createPartyId(objectFactory, "domibus-red");

        new Expectations(submissionAS4Transformer) {{
            userMessage.getCollaborationInfo();
            result = collaborationInfo;

            userMessage.getCollaborationInfo().getService().getValue();
            result = service;

            userMessage.getCollaborationInfo().getService().getType();
            result = serviceType;

            collaborationInfo.getAction();
            result = action;

            userMessage.getPartyInfo().getFrom().getRole();
            result = fromRole;

            userMessage.getPartyInfo().getTo().getRole();
            result = toRole;

            userMessage.getPayloadInfo().getPartInfo();
            result = partInfoList;

            userMessage.getPartyInfo().getFrom().getPartyId();
            result = fromPartyIdSet;

            userMessage.getPartyInfo().getTo().getPartyId();
            result = toPartyIdSet;

        }};

        final Submission submission = submissionAS4Transformer.transformFromMessaging(userMessage);
        Assert.assertNotNull(submission);

        new Verifications() {{
            Submission submissionActual;
            PartInfo partInfoActual;
            submissionAS4Transformer.addPayload(submissionActual = withCapture(), partInfoActual = withCapture());
            Assert.assertNotNull(submissionActual);
            Assert.assertNotNull(partInfoActual);
            times = 1;
        }};
    }



    @Test
    public void testAddPayload() {
        final String fileNameWithoutPath = createFileName();
        final String fileNameFull = createFileNameWithFullPath(fileNameWithoutPath);

        final Submission submission = new Submission();
        final PartInfo partInfo = objectFactory.createPartInfo();
        final PartProperties partProperties = objectFactory.createPartProperties();
        final Property property = new Property();
        property.setValue("MimeType");
        property.setName("text/xml");
        property.setType("string");
        partProperties.getProperties().add(property);
        partInfo.setPartProperties(partProperties);
        partInfo.setFileName(fileNameFull);

        //tested method
        submissionAS4Transformer.addPayload(submission, partInfo);

        Assert.assertNotNull(submission);
        Assert.assertTrue(submission.getPayloads().size() == 1);
        Submission.Payload payload = submission.getPayloads().iterator().next();
        Assert.assertTrue(payload.getPayloadProperties().size() == 2);
        Iterator<Submission.TypedProperty> typedProperties = payload.getPayloadProperties().iterator();
        Submission.TypedProperty typedProperty = typedProperties.next();
        Submission.TypedProperty typedProperty2 = typedProperties.next();

        Assert.assertEquals("text/xml", typedProperty.getKey());
        Assert.assertEquals("MimeType", typedProperty.getValue());

        Assert.assertTrue(typedProperty2.getKey().equals("FileName"));
        Assert.assertEquals(fileNameWithoutPath, typedProperty2.getValue());

    }



    @Test
    public void testTransformFromMessaging_NullUserMessage_TransformationNOK() {

        final UserMessage userMessage = null;

        final Submission submission = submissionAS4Transformer.transformFromMessaging(userMessage);
        Assert.assertNotNull(submission);
    }

    private String createFileName() {
        return UUID.randomUUID() + ".payload";
    }

    private String createFileNameWithFullPath(String fileNameWithoutPath) {
        return File.separator + "domibus" + File.separator + "payloads" + File.separator + fileNameWithoutPath;
    }

    private Set<PartyId> createPartyId(ObjectFactory objectFactory, String partyIdValue) {
        final PartyId partyId = objectFactory.createPartyId();
        partyId.setValue(partyIdValue);
        partyId.setType("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        return Collections.singleton(partyId);
    }
}