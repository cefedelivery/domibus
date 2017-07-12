package eu.domibus.common.model;

import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.Error;
import org.junit.Assert;
import org.junit.Test;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by migueti on 12/07/2017.
 */
public class ObjectFactoryTest {

    @Test
    public void testDescription() {
        Description description = new ObjectFactory().createDescription();
        Assert.assertNotNull(description);
        Assert.assertNull(description.getLang());
        Assert.assertNull(description.getValue());
        description.setLang("lang1");
        Assert.assertEquals("lang1", description.getLang());
        description.setValue("value1");
        Assert.assertEquals("value1", description.getValue());
    }

    @Test
    public void testCreatePullRequest() {
        PullRequest pullRequest = new ObjectFactory().createPullRequest();
        Assert.assertNotNull(pullRequest);
        Assert.assertNull(pullRequest.getMpc());
        Assert.assertTrue(pullRequest.getOtherAttributes().isEmpty());
        pullRequest.setMpc("mpc1");
        Assert.assertEquals("mpc1", pullRequest.getMpc());
    }

    @Test
    public void testCreateAgreementRef() {
        AgreementRef agreementRef = new ObjectFactory().createAgreementRef();
        Assert.assertNotNull(agreementRef);
        Assert.assertNull(agreementRef.getPmode());
        Assert.assertNull(agreementRef.getType());
        Assert.assertNull(agreementRef.getValue());
        agreementRef.setPmode("pmode1");
        Assert.assertEquals("pmode1", agreementRef.getPmode());
        agreementRef.setType("type1");
        Assert.assertEquals("type1", agreementRef.getType());
        agreementRef.setValue("value1");
        Assert.assertEquals("value1", agreementRef.getValue());
    }

    @Test
    public void testCreatePartProperties() {
        PartProperties partProperties = new ObjectFactory().createPartProperties();
        Assert.assertNotNull(partProperties);
        Assert.assertTrue(partProperties.getProperties().isEmpty());
    }

    @Test
    public void testCreateProperty() {
        Property property = new ObjectFactory().createProperty();
        Assert.assertNotNull(property);
        Assert.assertNull(property.getName());
        Assert.assertNull(property.getType());
        Assert.assertNull(property.getValue());
        Assert.assertEquals(0,property.getEntityId());
        property.setName("name1");
        Assert.assertEquals("name1", property.getName());
        property.setType("type1");
        Assert.assertEquals("type1", property.getType());
        property.setValue("value1");
        Assert.assertEquals("value1", property.getValue());
    }

    @Test
    public void testCreateMessageProperties() {
        MessageProperties messageProperties = new ObjectFactory().createMessageProperties();
        Assert.assertNotNull(messageProperties);
        Assert.assertTrue(messageProperties.getProperty().isEmpty());
    }

    @Test
    public void testCreateError() {
        Error error = new ObjectFactory().createError();
        Assert.assertNotNull(error);
        Assert.assertNull(error.getCategory());
        Assert.assertNull(error.getDescription());
        Assert.assertNull(error.getErrorCode());
        Assert.assertNull(error.getErrorDetail());
        Assert.assertNull(error.getOrigin());
        Assert.assertNull(error.getRefToMessageInError());
        Assert.assertNull(error.getSeverity());
        Assert.assertNull(error.getShortDescription());
        Assert.assertEquals(0, error.getEntityId());
        error.setCategory("category1");
        Assert.assertEquals("category1", error.getCategory());
        Description description = new Description();
        error.setDescription(description);
        Assert.assertEquals(description, error.getDescription());
        error.setErrorCode("errorcode1");
        Assert.assertEquals("errorcode1", error.getErrorCode());
        error.setErrorDetail("errordetail1");
        Assert.assertEquals("errordetail1", error.getErrorDetail());
        error.setOrigin("origin1");
        Assert.assertEquals("origin1", error.getOrigin());
        error.setRefToMessageInError("reftomessageinerror1");
        Assert.assertEquals("reftomessageinerror1", error.getRefToMessageInError());
        error.setSeverity("severity1");
        Assert.assertEquals("severity1", error.getSeverity());
        error.setShortDescription("shortdescription1");
        Assert.assertEquals("shortdescription1", error.getShortDescription());
    }

    @Test
    public void testCreatePaylodInfo() {
        PayloadInfo payloadInfo = new ObjectFactory().createPayloadInfo();
        Assert.assertNotNull(payloadInfo);
        Assert.assertTrue(payloadInfo.getPartInfo().isEmpty());
    }

    @Test
    public void testCreateSignalMessage() {
        SignalMessage signalMessage = new ObjectFactory().createSignalMessage();
        Assert.assertNotNull(signalMessage);
        Assert.assertTrue(signalMessage.getAny().isEmpty());
        Assert.assertTrue(signalMessage.getError().isEmpty());
        Assert.assertNull(signalMessage.getMessageInfo());
        Assert.assertNull(signalMessage.getPullRequest());
        Assert.assertNull(signalMessage.getReceipt());
        Assert.assertEquals(0, signalMessage.getEntityId());
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setMessageId("1");
        signalMessage.setMessageInfo(messageInfo);
        Assert.assertEquals(messageInfo, signalMessage.getMessageInfo());
        PullRequest pullRequest = new PullRequest();
        pullRequest.setMpc("mpc1");
        signalMessage.setPullRequest(pullRequest);
        Assert.assertEquals(pullRequest, signalMessage.getPullRequest());
        Receipt receipt = new Receipt();
        receipt.setEntityId(678);
        signalMessage.setReceipt(receipt);
        Assert.assertEquals(receipt, signalMessage.getReceipt());
    }

    @Test
    public void testCreatePartInfo() throws MalformedURLException {
        PartInfo partInfo = new ObjectFactory().createPartInfo();
        Assert.assertNotNull(partInfo);
        Assert.assertNull(partInfo.getBinaryData());
        Assert.assertNull(partInfo.getDescription());
        Assert.assertNull(partInfo.getFileName());
        Assert.assertNull(partInfo.getHref());
        Assert.assertNull(partInfo.getMime());
        Assert.assertNull(partInfo.getPartProperties());
        Assert.assertNull(partInfo.getPayloadDatahandler());
        Assert.assertNull(partInfo.getSchema());
        Assert.assertEquals(0, partInfo.getEntityId());
        byte[] byteA = new byte[]{1,0,1};
        partInfo.setBinaryData(byteA);
        Assert.assertEquals(byteA, partInfo.getBinaryData());
        Description description = new Description();
        description.setValue("value1");
        partInfo.setDescription(description);
        Assert.assertEquals(description, partInfo.getDescription());
        partInfo.setFileName("filename1");
        Assert.assertEquals("filename1", partInfo.getFileName());
        partInfo.setHref("href1");
        Assert.assertEquals("href1", partInfo.getHref());
        partInfo.setMime("mime1");
        Assert.assertEquals("mime1", partInfo.getMime());
        PartProperties partProperties = new PartProperties();
        partInfo.setPartProperties(partProperties);
        Assert.assertEquals(partProperties, partInfo.getPartProperties());
        DataHandler dataHandler = new DataHandler(new URL("http://www.google.be"));
        partInfo.setPayloadDatahandler(dataHandler);
        Assert.assertEquals(dataHandler, partInfo.getPayloadDatahandler());
        Schema schema = new Schema();
        schema.setLocation("location1");
        schema.setNamespace("namespace1");
        schema.setVersion("version1");
        partInfo.setSchema(schema);
        Assert.assertEquals(schema, partInfo.getSchema());
    }

    @Test
    public void testCreateReceipt() {
        Receipt receipt = new ObjectFactory().createReceipt();
        Assert.assertNotNull(receipt);
        Assert.assertTrue(receipt.getAny().isEmpty());
        Assert.assertEquals(0, receipt.getEntityId());
        receipt.setEntityId(1);
        Assert.assertEquals(1, receipt.getEntityId());
    }
}
