package eu.domibus.plugin.fs;

import eu.domibus.plugin.Submission;
import eu.domibus.plugin.fs.ebms3.CollaborationInfo;
import eu.domibus.plugin.fs.ebms3.From;
import eu.domibus.plugin.fs.ebms3.Property;
import eu.domibus.plugin.fs.ebms3.To;
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

    private static final String DOMIBUS_BLUE = "domibus-blue";
    private static final String DOMIBUS_RED = "domibus-red";
    private static final String ACTION_TC1LEG1 = "TC1Leg1";
    private static final String INITIATOR_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
    private static final String RESPONDER_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";
    private static final String PAYLOAD_ID = "cid:message";
    private static final String UNREGISTERED_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    private static final String ORIGINAL_SENDER = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
    private static final String FINAL_RECIPIENT = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
    public static final String PROPERTY_ORIGINAL_SENDER = "originalSender";
    public static final String PROPERTY_FINAL_RECIPIENT = "finalRecipient";
    private static final String SERVICE_NOPROCESS = "bdx:noprocess";
    private static final String SERVICE_TYPE_TC1 = "tc1";
    private static final String MIME_TYPE = "MimeType";
    private static final String DEFAULT_MT = "text/xml";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void transformFromSubmissionTest() throws Exception {
        // Submission
        Submission submission = new Submission();
        submission.addFromParty(DOMIBUS_BLUE, UNREGISTERED_PARTY_TYPE);
        submission.setFromRole(INITIATOR_ROLE);
        submission.addToParty(DOMIBUS_RED, UNREGISTERED_PARTY_TYPE);
        submission.setToRole(RESPONDER_ROLE);

        submission.setServiceType(SERVICE_TYPE_TC1);
        submission.setService(SERVICE_NOPROCESS);
        submission.setAction(ACTION_TC1LEG1);

        submission.setConversationId("123");
        submission.setMessageId("1234");

        submission.addMessageProperty(PROPERTY_ORIGINAL_SENDER, ORIGINAL_SENDER);
        submission.addMessageProperty(PROPERTY_FINAL_RECIPIENT, FINAL_RECIPIENT);
        submission.setAgreementRefType("");
        submission.setAgreementRef("A1");

        String payload = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
        DataHandler payLoadDataHandler = new DataHandler(new ByteArrayDataSource(payload.getBytes(), DEFAULT_MT));
        Submission.TypedProperty submissionTypedProperty = new Submission.TypedProperty(MIME_TYPE, DEFAULT_MT);
        Collection<Submission.TypedProperty> listTypedProperty = new ArrayList<>();
        listTypedProperty.add(submissionTypedProperty);
        Submission.Payload submissionPayload = new Submission.Payload(PAYLOAD_ID, payLoadDataHandler, listTypedProperty, false, null, null);
        submission.addPayload(submissionPayload);

        FSMessageTransformer transformer = new FSMessageTransformer();
        FSMessage result = transformer.transformFromSubmission(submission, null);

        // User message
        From metadataFrom = result.getMetadata().getPartyInfo().getFrom();
        Assert.assertEquals(UNREGISTERED_PARTY_TYPE, metadataFrom.getPartyId().getType());
        Assert.assertEquals(DOMIBUS_BLUE, metadataFrom.getPartyId().getValue());
        Assert.assertEquals(INITIATOR_ROLE, metadataFrom.getRole());

        To metadataTo = result.getMetadata().getPartyInfo().getTo();
        Assert.assertEquals(UNREGISTERED_PARTY_TYPE, metadataTo.getPartyId().getType());
        Assert.assertEquals(DOMIBUS_RED, metadataTo.getPartyId().getValue());
        Assert.assertEquals(RESPONDER_ROLE, metadataTo.getRole());

        CollaborationInfo metadataCollabInfo = result.getMetadata().getCollaborationInfo();
        Assert.assertEquals(SERVICE_TYPE_TC1, metadataCollabInfo.getService().getType());
        Assert.assertEquals(SERVICE_NOPROCESS, metadataCollabInfo.getService().getValue());
        Assert.assertEquals(ACTION_TC1LEG1, metadataCollabInfo.getAction());

        List<Property> metadataMessageProperties = result.getMetadata().getMessageProperties().getProperty();
        // TODO: WIP
    }

    @Test
    public void transformToSubmission() throws Exception {

    }



}