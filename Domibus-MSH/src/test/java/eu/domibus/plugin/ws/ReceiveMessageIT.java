package eu.domibus.plugin.ws;

import eu.domibus.AbstractIT;
import eu.domibus.common.MessageStatus;
import eu.domibus.ebms3.receiver.SetPolicyInInterceptor;
import eu.domibus.ebms3.sender.MSHDispatcher;
import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Provider;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * This class implements the test cases Receive Message-01 and Receive Message-02.
 *
 * @author draguio
 * @author martifp
 */
@ContextConfiguration("classpath:pmode-dao.xml")
public class ReceiveMessageIT extends AbstractIT {

    private static boolean initialized;
    @Autowired
    Provider<SOAPMessage> mshWebservice;
    @Autowired
    SetPolicyInInterceptor setPolicyInInterceptor;

    @Before
    public void before() throws IOException {

        if (!initialized) {
            // The dataset is executed only once for each class
            insertDataset("sendMessageDataset.sql");
            initialized = true;
        }
    }

    private void verifyMessageStatus(String messageId) throws SQLException {
        Connection con = dataSource.getConnection();
        String sql = "SELECT MESSAGE_ID, MESSAGE_STATUS FROM TB_MESSAGE_LOG WHERE MESSAGE_TYPE like 'USER_MESSAGE' AND MESSAGE_ID = ?";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setString(1, messageId);
        ResultSet resultSet = pstmt.executeQuery();
        resultSet.next();
        Assert.assertEquals(resultSet.getString("MESSAGE_STATUS"), MessageStatus.RECEIVED.name());
        pstmt.close();
    }

    private void verifySignalMessageStatus(String messageId) throws SQLException {
        Connection con = dataSource.getConnection();
        String sql = "SELECT MESSAGE_ID FROM TB_MESSAGE_INFO WHERE REF_TO_MESSAGE_ID= ?";
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setString(1, messageId);
        ResultSet resultSet = pstmt.executeQuery();
        resultSet.next();

        sql = "SELECT MESSAGE_STATUS FROM TB_MESSAGE_LOG WHERE MESSAGE_TYPE like 'SIGNAL_MESSAGE' AND MESSAGE_ID = ?";
        pstmt = con.prepareStatement(sql);
        pstmt.setString(1, resultSet.getString("MESSAGE_ID"));
        resultSet = pstmt.executeQuery();
        resultSet.next();
        Assert.assertEquals(resultSet.getString("MESSAGE_STATUS"), MessageStatus.SEND_IN_PROGRESS.name());
        pstmt.close();
    }

    /**
     * This test invokes the MSHWebService with a mocked SOAPMessage and verifies that the message is stored
     * in the database with the status RECEIVED
     * The test hooks after the SetPolicyInInterceptor, therefore PMODE_KEY_CONTEXT_PROPERTY needs to be set manually
     *
     * @throws SOAPException, IOException, SQLException, ParserConfigurationException, SAXException
     *                        <p>
     *                        ref: Receive Message-01
     */
    @Test
    @Transactional
    public void testReceiveMessage() throws SOAPException, IOException, SQLException, ParserConfigurationException, SAXException {
        String filename = "SOAPMessage2.xml";
        String messageId = "43bb6883-77d2-4a41-bac4-52a485d50084@domibus.eu";
        SOAPMessage soapMessage = createSOAPMessage(filename);
        mshWebservice.invoke(soapMessage);
        verifyMessageStatus(messageId);
        verifySignalMessageStatus(messageId);
    }

    protected SOAPMessage createSOAPMessage(String dataset) throws SOAPException, IOException, ParserConfigurationException, SAXException {

        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage message = factory.createMessage();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document document = builder.parse(new File("target/test-classes/dataset/as4/" + dataset).getAbsolutePath());
        DOMSource domSource = new DOMSource(document);
        SOAPPart soapPart = message.getSOAPPart();
        soapPart.setContent(domSource);

        AttachmentPart attachment = message.createAttachmentPart();
        attachment.setContent(Base64.decodeBase64("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=".getBytes()), "text/xml");
        attachment.setContentId("sbdh-order");
        message.addAttachmentPart(attachment);

        message.setProperty(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY, "blue_gw:red_gw:testService1:tc1Action::pushTestcase1tc1Action");
        try {
            SOAPHeader soapHeader = message.getSOAPHeader();
        } catch (Exception e) {

        }
        return message;
    }

    /**
     * This test invokes the MSHWebService with a mocked SOAPMessage and verifies that the message is stored
     * in the database with the status RECEIVED. The SOAP Message is built with the Policy interceptor.
     *
     * @throws IOException, SOAPException, SQLException
     *                      <p/>
     *                      ref: Receive Message-02
     */
    @Test
    @Transactional
    public void testReceiveMessageWithPolicy() throws IOException, SOAPException, SQLException, ParserConfigurationException, SAXException {
        String filename = "SOAPMessage.xml";
        String messageId = "359b840b-b215-4c70-89e7-59aa0fe73cec@domibus.eu";
        SOAPMessage soapMessage = createSOAPMessagePolicyInterceptor(filename);
        soapMessage.setProperty(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY, "blue_gw:red_gw:testService1:tc1Action::pushTestcase1tc1Action");
        AttachmentPart attachment = soapMessage.createAttachmentPart();
        attachment.setContent(Base64.decodeBase64("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=".getBytes()), "text/xml");
        attachment.setContentId("sbdh-order");
        soapMessage.addAttachmentPart(attachment);

        mshWebservice.invoke(soapMessage);

        verifyMessageStatus(messageId);
    }

    @Test
    @Transactional
    public void testReceivePingMessage() throws IOException, SOAPException, SQLException, ParserConfigurationException, SAXException {
        String filename = "SOAPPingMessage.xml";
        String messageId = "ping123@domibus.eu";
        SOAPMessage soapMessage = createSOAPMessage(filename);
        SOAPMessage responseMessage = mshWebservice.invoke(soapMessage);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        responseMessage.writeTo(out);
        Assert.assertFalse(out.toString().contains("eb:Error"));
        try {
            verifyMessageStatus(messageId);
        }catch(SQLException e) {
            Assert.assertEquals("No data is available [2000-178]", e.getMessage());
        }
    }

    private SOAPMessage createSOAPMessagePolicyInterceptor(String dataset) throws SOAPException, IOException, ParserConfigurationException, SAXException {
        InputStream is = new FileInputStream(new File("target/test-classes/dataset/as4/" + dataset).getAbsolutePath());

        SoapMessage sm = new SoapMessage(new MessageImpl());
        sm.setContent(InputStream.class, is);
        InterceptorChain ic = new PhaseInterceptorChain((new PhaseManagerImpl()).getOutPhases());
        sm.setInterceptorChain(ic);
        ExchangeImpl exchange = new ExchangeImpl();
        Bus bus = new ExtensionManagerBus();
        bus.setExtension(new PolicyBuilderImpl(bus), PolicyBuilder.class);
        exchange.put(Bus.class, bus);
        sm.setExchange(exchange);

        setPolicyInInterceptor.handleMessage(sm);

        //return sm.getContent(SOAPMessage.class); // TODO is returns null

        SOAPMessage message = createSOAPMessage("SOAPMessage.xml");
        return message;
    }
}
