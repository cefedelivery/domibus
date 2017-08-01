package eu.domibus;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import eu.domibus.ebms3.sender.NonRepudiationChecker;
import eu.domibus.ebms3.sender.ReliabilityChecker;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.PayloadType;
import eu.domibus.plugin.webService.generated.SendRequest;
import eu.domibus.plugin.webService.generated.SendResponse;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Rule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.NodeList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Created by draguio on 18/02/2016.
 */
public abstract class AbstractSendMessageIT extends AbstractIT{

    public static final String STRING_TYPE = "string";
    public static final String INT_TYPE = "int";
    public static final String BOOLEAN_TYPE = "boolean";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(SERVICE_PORT);
    @Autowired
    BackendInterface backendWebService;
    @Mock
    NonRepudiationChecker nonRepudiationChecker;

    @InjectMocks
    @Autowired
    private ReliabilityChecker reliabilityChecker;


    public void prepareSendMessage(String responseFileName) {
        /* Initialize the mock objects */
        MockitoAnnotations.initMocks(this);
        /* Mock the nonRepudiationChecker, it fails because security in/out policy interceptors are not ran */
        Mockito.when(nonRepudiationChecker.compareUnorderedReferenceNodeLists(Mockito.any(NodeList.class), Mockito.any(NodeList.class))).thenReturn(true);

        String body = getAS4Response(responseFileName);

        // Mock the response from the recipient MSH
        stubFor(post(urlEqualTo("/domibus/services/msh"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/soap+xml")
                        .withBody(body)));
    }

    protected void verifySendMessageAck(SendResponse response) throws InterruptedException, SQLException{
        // Required in order to let time to the message to be consumed
        TimeUnit.SECONDS.sleep(4);

        Assert.assertNotNull(response);
        String messageId = response.getMessageID().get(0);

        verify(postRequestedFor(urlMatching("/domibus/services/msh"))
                .withRequestBody(matching(".*"))
                .withHeader("Content-Type", notMatching("application/soap+xml")));

        Connection con = dataSource.getConnection();
        String sql = "SELECT MESSAGE_ID, MESSAGE_STATUS FROM TB_MESSAGE_LOG WHERE MESSAGE_ID = ?";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setString(1, messageId);
        ResultSet resultSet = pstmt.executeQuery();
        resultSet.next();
        Assert.assertEquals(MessageStatus.ACKNOWLEDGED.name(), resultSet.getString("MESSAGE_STATUS"));
        pstmt.close();
    }

    protected Messaging createMessageHeader(String payloadHref) {
        return createMessageHeader(payloadHref, "text/xml");
    }

    protected Messaging createMessageHeader(String payloadHref, String mimeType) {
        Messaging ebMSHeaderInfo = new Messaging();
        UserMessage userMessage = new UserMessage();
        CollaborationInfo collaborationInfo = new CollaborationInfo();
        collaborationInfo.setAction("TC1Leg1");
        AgreementRef agreementRef = new AgreementRef();
        agreementRef.setValue("EDELIVERY-1110");
        collaborationInfo.setAgreementRef(agreementRef);
        Service service = new Service();
        service.setValue("bdx:noprocess");
        service.setType("tc1");
        collaborationInfo.setService(service);
        userMessage.setCollaborationInfo(collaborationInfo);
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.getProperty().add(createProperty("originalSender", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1", STRING_TYPE));
        messageProperties.getProperty().add(createProperty("finalRecipient", "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C41", STRING_TYPE));
        userMessage.setMessageProperties(messageProperties);
        PartyInfo partyInfo = new PartyInfo();
        From from = new From();
        from.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        PartyId sender = new PartyId();
        sender.setValue("urn:oasis:names:tc:ebcore:partyid-type:unregistered:domibus-blue");
        from.setPartyId(sender);
        partyInfo.setFrom(from);
        To to = new To();
        to.setRole("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        PartyId receiver = new PartyId();
        receiver.setValue("urn:oasis:names:tc:ebcore:partyid-type:unregistered:domibus-red");
        to.setPartyId(receiver);
        partyInfo.setTo(to);
        userMessage.setPartyInfo(partyInfo);
        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = new PartInfo();
        Description description = new Description();
        description.setValue("e-sens-sbdh-order");
        description.setLang("en-US");
        partInfo.setHref(payloadHref);
        if(mimeType != null) {
            PartProperties partProperties = new PartProperties();
            partProperties.getProperty().add(createProperty(mimeType, "MimeType", STRING_TYPE));
            partInfo.setPartProperties(partProperties);
        }
        partInfo.setDescription(description);
        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        ebMSHeaderInfo.setUserMessage(userMessage);
        return ebMSHeaderInfo;
    }

    protected Property createProperty(String name, String value, String type) {
        Property aProperty = new Property();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(type);
        return aProperty;
    }

    protected SendRequest createSendRequest(String payloadHref) {
        SendRequest sendRequest = new SendRequest();
        PayloadType payload = new PayloadType();
        payload.setPayloadId(payloadHref);
        payload.setContentType("text/xml");
        payload.setValue(Base64.decodeBase64("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=".getBytes()));
        sendRequest.getPayload().add(payload);

        payload = new PayloadType();
        payload.setPayloadId(payloadHref);
        payload.setValue(Base64.decodeBase64("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=".getBytes()));
        sendRequest.setBodyload(payload);
        return sendRequest;
    }
}
