package eu.domibus.plugin.fs;

import eu.domibus.plugin.Submission;
import eu.domibus.plugin.fs.ebms3.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSMessageTransformerTest {

    private static final String INITIATOR_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
    private static final String RESPONDER_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";

    private static final String UNREGISTERED_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String ORIGINAL_SENDER = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
    private static final String PROPERTY_ORIGINAL_SENDER = "originalSender";
    private static final String FINAL_RECIPIENT = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
    private static final String PROPERTY_FINAL_RECIPIENT = "finalRecipient";

    private static final String DOMIBUS_BLUE = "domibus-blue";
    private static final String DOMIBUS_RED = "domibus-red";

    private static final String SERVICE_NOPROCESS = "bdx:noprocess";
    private static final String PAYLOAD_ID = "cid:message";
    private static final String ACTION_TC1LEG1 = "TC1Leg1";
    private static final String SERVICE_TYPE_TC1 = "tc1";
    private static final String MIME_TYPE = "MimeType";
    private static final String TEXT_XML = "text/xml";
    private static final String AGREEMENT_REF_A1 = "A1";
    private static final String EMPTY_STR = "";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testTransformFromSubmissionNormalFlow() throws Exception {
        String messageId = "3c5558e4-7b6d-11e7-bb31-be2e44b06b34@domibus.eu";
        String conversationId = "ae413adb-920c-4d9c-a5a7-b5b2596eaf1c@domibus.eu";
        String payloadContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";

        // Submission
        Submission submission = new Submission();
        submission.setMessageId(messageId);
        submission.addFromParty(DOMIBUS_BLUE, UNREGISTERED_PARTY_TYPE);
        submission.setFromRole(INITIATOR_ROLE);
        submission.addToParty(DOMIBUS_RED, UNREGISTERED_PARTY_TYPE);
        submission.setToRole(RESPONDER_ROLE);

        submission.setServiceType(SERVICE_TYPE_TC1);
        submission.setService(SERVICE_NOPROCESS);
        submission.setAction(ACTION_TC1LEG1);
        submission.setAgreementRefType(EMPTY_STR);
        submission.setAgreementRef(AGREEMENT_REF_A1);
        submission.setConversationId(conversationId);

        submission.addMessageProperty(PROPERTY_ORIGINAL_SENDER, ORIGINAL_SENDER);
        submission.addMessageProperty(PROPERTY_FINAL_RECIPIENT, FINAL_RECIPIENT);

        DataHandler payLoadDataHandler = new DataHandler(new ByteArrayDataSource(payloadContent.getBytes(), TEXT_XML));
        Submission.TypedProperty submissionTypedProperty = new Submission.TypedProperty(MIME_TYPE, TEXT_XML);
        Collection<Submission.TypedProperty> listTypedProperty = new ArrayList<>();
        listTypedProperty.add(submissionTypedProperty);
        Submission.Payload submissionPayload = new Submission.Payload(PAYLOAD_ID, payLoadDataHandler, listTypedProperty, false, null, null);
        submission.addPayload(submissionPayload);

        // Transform FSMessage from Submission
        FSMessageTransformer transformer = new FSMessageTransformer();
        FSMessage fsMessage = transformer.transformFromSubmission(submission, null);

        // Expected results for FSMessage
        UserMessage userMessage = fsMessage.getMetadata();
        From from = userMessage.getPartyInfo().getFrom();
        Assert.assertEquals(UNREGISTERED_PARTY_TYPE, from.getPartyId().getType());
        Assert.assertEquals(DOMIBUS_BLUE, from.getPartyId().getValue());
        Assert.assertEquals(INITIATOR_ROLE, from.getRole());

        To to = userMessage.getPartyInfo().getTo();
        Assert.assertEquals(UNREGISTERED_PARTY_TYPE, to.getPartyId().getType());
        Assert.assertEquals(DOMIBUS_RED, to.getPartyId().getValue());
        Assert.assertEquals(RESPONDER_ROLE, to.getRole());

        CollaborationInfo collaborationInfo = userMessage.getCollaborationInfo();
        Assert.assertEquals(SERVICE_TYPE_TC1, collaborationInfo.getService().getType());
        Assert.assertEquals(SERVICE_NOPROCESS, collaborationInfo.getService().getValue());
        Assert.assertEquals(ACTION_TC1LEG1, collaborationInfo.getAction());
        Assert.assertEquals(EMPTY_STR, collaborationInfo.getAgreementRef().getType());
        Assert.assertEquals(AGREEMENT_REF_A1, collaborationInfo.getAgreementRef().getValue());
        Assert.assertEquals(conversationId, collaborationInfo.getConversationId());

        List<Property> propertyList = userMessage.getMessageProperties().getProperty();
        Assert.assertEquals(2, propertyList.size());
        Property property0 = propertyList.get(0);
        Assert.assertEquals(PROPERTY_ORIGINAL_SENDER, property0.getName());
        Assert.assertEquals(ORIGINAL_SENDER, property0.getValue());
        Property property1 = propertyList.get(1);
        Assert.assertEquals(PROPERTY_FINAL_RECIPIENT, property1.getName());
        Assert.assertEquals(FINAL_RECIPIENT, property1.getValue());

        /*
        MessageInfo messageInfo = userMessage.getMessageInfo();
        Assert.assertEquals(messageId, messageInfo.getMessageId());
        */

        DataHandler dataHandler = fsMessage.getDataHandler();
        Assert.assertEquals(TEXT_XML, dataHandler.getContentType());
        Assert.assertEquals(payloadContent, dataHandler.getContent());
    }

    @Test
    public void testTransformToSubmissionNormalFlow() throws Exception {
        String payloadContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
        UserMessage metadata = FSTestHelper.getUserMessage(this.getClass(), "testTransformToSubmissionNormalFlow_metadata.xml");

        DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(payloadContent.getBytes(), TEXT_XML));
        FSMessage fsMessage = new FSMessage(dataHandler, metadata);

        // Transform FSMessage to Submission
        FSMessageTransformer transformer = new FSMessageTransformer();
        Submission submission = transformer.transformToSubmission(fsMessage);

        Assert.assertNotNull(submission);
        Assert.assertEquals(1, submission.getFromParties().size());
        Submission.Party fromParty = submission.getFromParties().iterator().next();
        Assert.assertEquals(DOMIBUS_BLUE, fromParty.getPartyId());
        Assert.assertEquals(UNREGISTERED_PARTY_TYPE, fromParty.getPartyIdType());
        Assert.assertEquals(INITIATOR_ROLE, submission.getFromRole());

        Assert.assertEquals(1, submission.getToParties().size());
        Submission.Party toParty = submission.getToParties().iterator().next();
        Assert.assertEquals(DOMIBUS_RED, toParty.getPartyId());
        Assert.assertEquals(UNREGISTERED_PARTY_TYPE, toParty.getPartyIdType());
        Assert.assertEquals(RESPONDER_ROLE, submission.getToRole());

        Assert.assertNull(submission.getAgreementRefType());
        Assert.assertNull(submission.getAgreementRef());
        Assert.assertEquals(SERVICE_NOPROCESS, submission.getService());
        Assert.assertEquals(SERVICE_TYPE_TC1, submission.getServiceType());
        Assert.assertEquals(ACTION_TC1LEG1, submission.getAction());

        Assert.assertEquals(2, submission.getMessageProperties().size());
        for (Submission.TypedProperty typedProperty : submission.getMessageProperties()) {
            if (PROPERTY_ORIGINAL_SENDER.equalsIgnoreCase(typedProperty.getKey())) {
                Assert.assertEquals(ORIGINAL_SENDER, typedProperty.getValue());
            }
            if (PROPERTY_FINAL_RECIPIENT.equalsIgnoreCase(typedProperty.getKey())) {
                Assert.assertEquals(FINAL_RECIPIENT, typedProperty.getValue());
            }
        }

        Assert.assertEquals(1, submission.getPayloads().size());
        Submission.Payload submissionPayload = submission.getPayloads().iterator().next();
        Submission.TypedProperty payloadProperty = submissionPayload.getPayloadProperties().iterator().next();
        Assert.assertEquals(MIME_TYPE, payloadProperty.getKey());
        Assert.assertEquals(TEXT_XML, payloadProperty.getValue());

        DataHandler payloadDatahandler = submissionPayload.getPayloadDatahandler();
        Assert.assertEquals(TEXT_XML, payloadDatahandler.getContentType());
        Assert.assertEquals(payloadContent, payloadDatahandler.getContent());
    }

}