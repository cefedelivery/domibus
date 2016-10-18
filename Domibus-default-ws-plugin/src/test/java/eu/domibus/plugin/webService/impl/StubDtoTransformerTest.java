package eu.domibus.plugin.webService.impl;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.*;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory;
import eu.domibus.plugin.webService.generated.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import eu.domibus.plugin.Submission;



public class StubDtoTransformerTest
{
    private static final Log LOG = LogFactory.getLog(StubDtoTransformerTest.class);

    private static final String MIME_TYPE               = "MimeType";
    private static final String DEFAULT_MT              = "text/xml";
    private static final String DOMIBUS_BLUE            = "domibus-blue";
    private static final String DOMIBUS_RED             = "domibus-red";
    private static final String INITIATOR_ROLE          = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator";
    private static final String RESPONDEER_ROLE         = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";
    private static final String PAYLOAD_ID              = "cid:message";
    private static final String UNREGISTERED_PARTY_TYPE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";

    /**
     * Testing Basic happy flow scenario of transform from Messaging to Submission class
     * for ws plugin implementation of Domibus!
     */
    @Test
    public void testTransformFromMessaging_HappyFlow()
    {
	LOG.info("Started with test case: testTransformFromMessaging_HappyFlow");

	UserMessage userMessageObj = new UserMessage();

        /*UserMessage.MessageInfo population start*/
	MessageInfo messageInfoObj = new MessageInfo();
	messageInfoObj.setTimestamp(new XMLGregorianCalendarImpl(new GregorianCalendar()));
	userMessageObj.setMessageInfo(messageInfoObj);
	/*UserMessage.MessageInfo population end*/

	/*UserMessage.PartyInfo population start*/
	PartyInfo objPartyInfo = new PartyInfo();

	PartyId fromPartyIdObj = new PartyId();
	fromPartyIdObj.setValue(DOMIBUS_BLUE);
	fromPartyIdObj.setType(UNREGISTERED_PARTY_TYPE);

	From fromObj = new From();
	fromObj.setPartyId(fromPartyIdObj);
	fromObj.setRole(INITIATOR_ROLE);

	PartyId toPartyIdObj = new PartyId();
	toPartyIdObj.setValue(DOMIBUS_RED);
	toPartyIdObj.setType(UNREGISTERED_PARTY_TYPE);

	To toObj = new To();
	toObj.setPartyId(toPartyIdObj);
	toObj.setRole(RESPONDEER_ROLE);

	objPartyInfo.setFrom(fromObj);
	objPartyInfo.setTo(toObj);
	userMessageObj.setPartyInfo(objPartyInfo);
	/*UserMessage.PartyInfo population end*/

	/*UserMessage.CollaborationInfo population start*/
	CollaborationInfo objCollaborationInfo = new CollaborationInfo();

	Service serviceObj = new Service();
	serviceObj.setValue("bdx:noprocess");
	serviceObj.setType("tc1");

	objCollaborationInfo.setService(serviceObj);
	objCollaborationInfo.setAction("TC1Leg1");
	userMessageObj.setCollaborationInfo(objCollaborationInfo);
	/*UserMessage.CollaborationInfo population end*/

	/*UserMessage.PayLoadInfo population start*/
	Property objProperty = new Property();
	objProperty.setName(MIME_TYPE);
	objProperty.setValue(DEFAULT_MT);

	PartProperties objPartProperties = new PartProperties();
	objPartProperties.getProperty().add(objProperty);
	PartInfo objPartInfo = new PartInfo();
	objPartInfo.setHref(PAYLOAD_ID);
	objPartInfo.setPartProperties(objPartProperties);

	eu.domibus.plugin.webService.generated.PayloadType objPayloadType = new PayloadType();
	objPayloadType.setPayloadId(PAYLOAD_ID);
	String strPayLoad = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
	objPayloadType.setValue(strPayLoad.getBytes());

	ExtendedPartInfo objExtendedPartInfo = new ExtendedPartInfo(objPartInfo);
	objExtendedPartInfo.setHref(PAYLOAD_ID);
	objExtendedPartInfo.setInBody(false);
	objExtendedPartInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(objPayloadType.getValue(), objPayloadType.getContentType() == null ? DEFAULT_MT : objPayloadType.getContentType())));

	PayloadInfo objPayloadInfo = new PayloadInfo();
	objPayloadInfo.getPartInfo().add(objExtendedPartInfo);
	userMessageObj.setPayloadInfo(objPayloadInfo);
	/*UserMessage.PayLoadInfo population end*/

	StubDtoTransformer testObj = new StubDtoTransformer();
	Submission objSubmission = testObj.transformFromMessaging(userMessageObj);

        Assert.assertNotNull("Submission object in the response should not be null:", objSubmission);

	LOG.info("Completed with test case: testTransformFromMessaging_HappyFlow");
    }


    /**
     * Testing transform from Messaging to Submission class for ws plugin implementation of Domibus!
     * Any leading/trailing white spaces in Messaging/UserMessage/PartyInfo/From/PartyId or
     * Messaging/UserMessage/PartyInfo/To/PartyId or Messaging/UserMessage/CollaborationInfo/Service
     * should be trimmed.
     */
    
    @Test
    public void testTransformFromMessaging_TrimWhiteSpace()
    {
        LOG.info("Started with test case: testTransformFromMessaging_TrimWhiteSpace");

        UserMessage userMessageObj = new UserMessage();

        /*UserMessage.MessageInfo population start*/
        MessageInfo messageInfoObj = new MessageInfo();
        messageInfoObj.setTimestamp(new XMLGregorianCalendarImpl(new GregorianCalendar()));
        userMessageObj.setMessageInfo(messageInfoObj);
	/*UserMessage.MessageInfo population end*/

	/*UserMessage.PartyInfo population start*/
        PartyInfo objPartyInfo = new PartyInfo();

        PartyId fromPartyIdObj = new PartyId();
        fromPartyIdObj.setValue('\t' + DOMIBUS_BLUE + "   ");
        fromPartyIdObj.setType("\t" + UNREGISTERED_PARTY_TYPE + "  ");

        From fromObj = new From();
        fromObj.setPartyId(fromPartyIdObj);
        fromObj.setRole("\t" + INITIATOR_ROLE + "  ");

        PartyId toPartyIdObj = new PartyId();
        toPartyIdObj.setValue("\t\t" + DOMIBUS_RED + "    ");
        toPartyIdObj.setType("\t   " + UNREGISTERED_PARTY_TYPE + "\t");

        To toObj = new To();
        toObj.setPartyId(toPartyIdObj);
        toObj.setRole("   " + RESPONDEER_ROLE + "\t\t");

        objPartyInfo.setFrom(fromObj);
        objPartyInfo.setTo(toObj);
        userMessageObj.setPartyInfo(objPartyInfo);
	/*UserMessage.PartyInfo population end*/

	/*UserMessage.CollaborationInfo population start*/
        CollaborationInfo objCollaborationInfo = new CollaborationInfo();

        Service serviceObj = new Service();
        serviceObj.setValue("\t" + "bdx:noprocess");
        serviceObj.setType("   "+"tc1"+"\t");

        objCollaborationInfo.setService(serviceObj);
        objCollaborationInfo.setAction("\t"+"TC1Leg1"+"  ");
        userMessageObj.setCollaborationInfo(objCollaborationInfo);
	/*UserMessage.CollaborationInfo population end*/

	/*UserMessage.PayLoadInfo population start*/
        Property objProperty = new Property();
        objProperty.setName(MIME_TYPE);
        objProperty.setValue(DEFAULT_MT);

        PartProperties objPartProperties = new PartProperties();
        objPartProperties.getProperty().add(objProperty);
        PartInfo objPartInfo = new PartInfo();
        objPartInfo.setHref(PAYLOAD_ID);
        objPartInfo.setPartProperties(objPartProperties);

        eu.domibus.plugin.webService.generated.PayloadType objPayloadType = new PayloadType();
        objPayloadType.setPayloadId(PAYLOAD_ID);
        String strPayLoad = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
        objPayloadType.setValue(strPayLoad.getBytes());

        ExtendedPartInfo objExtendedPartInfo = new ExtendedPartInfo(objPartInfo);
        objExtendedPartInfo.setHref(PAYLOAD_ID);
        objExtendedPartInfo.setInBody(false);
        objExtendedPartInfo.setPayloadDatahandler(new DataHandler(new ByteArrayDataSource(objPayloadType.getValue(), objPayloadType.getContentType() == null ? DEFAULT_MT : objPayloadType.getContentType())));

        PayloadInfo objPayloadInfo = new PayloadInfo();
        objPayloadInfo.getPartInfo().add(objExtendedPartInfo);
        userMessageObj.setPayloadInfo(objPayloadInfo);
	/*UserMessage.PayLoadInfo population end*/

        StubDtoTransformer testObj = new StubDtoTransformer();
        Submission objSubmission = testObj.transformFromMessaging(userMessageObj);

        Assert.assertNotNull("Submission object in the response should not be null:", objSubmission);
        for (Iterator<Submission.Party> itr = objSubmission.getFromParties().iterator(); itr.hasNext(); )
        {
	    Submission.Party fromPartyObj = itr.next();
	    Assert.assertEquals(DOMIBUS_BLUE, fromPartyObj.getPartyId());
	    Assert.assertEquals(UNREGISTERED_PARTY_TYPE, fromPartyObj.getPartyIdType());
        }
        Assert.assertEquals(INITIATOR_ROLE, objSubmission.getFromRole());

        for (Iterator<Submission.Party> itr = objSubmission.getToParties().iterator(); itr.hasNext(); )
        {
	    Submission.Party toPartyObj = itr.next();
	    Assert.assertEquals(DOMIBUS_RED, toPartyObj.getPartyId());
	    Assert.assertEquals(UNREGISTERED_PARTY_TYPE, toPartyObj.getPartyIdType());
        }
        Assert.assertEquals(RESPONDEER_ROLE, objSubmission.getToRole());

        Assert.assertEquals("bdx:noprocess", objSubmission.getService());
        Assert.assertEquals("tc1", objSubmission.getServiceType());
        Assert.assertEquals("TC1Leg1", objSubmission.getAction());

        for(Iterator<Submission.TypedProperty> itr = objSubmission.getMessageProperties().iterator(); itr.hasNext();)
        {
            Submission.TypedProperty prop = itr.next();
            Assert.assertEquals(MIME_TYPE, prop.getKey());
            Assert.assertEquals(DEFAULT_MT, prop.getValue());
        }
        LOG.info("Completed with test case: testTransformFromMessaging_TrimWhiteSpace");
    }

}
