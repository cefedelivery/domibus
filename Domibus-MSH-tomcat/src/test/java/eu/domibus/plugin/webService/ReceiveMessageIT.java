package eu.domibus.plugin.webService;

import eu.domibus.AbstractIT;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Provider;
import java.io.IOException;
import java.sql.SQLException;


/**
 * This class implements the test cases Receive Message-01 and Receive Message-02.
 *
 * @author draguio
 * @author martifp
 */
public class ReceiveMessageIT extends AbstractIT {

    @Autowired
    Provider<SOAPMessage> mshWebservice;

    @Before
    public void before() throws IOException, XmlProcessingException {
        final byte[] pmodeBytes = IOUtils.toByteArray(new ClassPathResource("dataset/pmode/PModeTemplate.xml").getInputStream());
        final Configuration pModeConfiguration = pModeProvider.getPModeConfiguration(pmodeBytes);
        configurationDAO.updateConfiguration(pModeConfiguration);
    }

    /**
     * This test invokes the MSHWebService and verifies that the message is stored
     * in the database with the status RECEIVED
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

        waitUntilMessageIsReceived(messageId);
    }

    @Test
    @Transactional
    public void testReceiveTestMessage() throws IOException, SOAPException, SQLException, ParserConfigurationException, SAXException, JAXBException {
        String filename = "SOAPTestMessage.xml";
        String messageId = "ping123@domibus.eu";
        SOAPMessage soapMessage = createSOAPMessage(filename);

        mshWebservice.invoke(soapMessage);
        waitUntilMessageIsReceived(messageId);
    }

}
