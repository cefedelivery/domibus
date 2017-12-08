package eu.domibus.ebms3.sender;

import eu.domibus.ebms3.SoapInterceptorTest;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.spring.SpringContextProvider;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.wss4j.policy.SPConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * @author idragusa
 * @since 4.0
 */
@RunWith(JMockit.class)
public class SetSignatureAlgorithmInInterceptorTest extends SoapInterceptorTest {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetSignatureAlgorithmInInterceptorTest.class);

    @Tested
    SetSignatureAlgorithmInInterceptor setSignatureAlgorithmInInterceptor;

    @Test
    public void testHandleMessage(@Mocked SpringContextProvider springContextProvider) throws XMLStreamException, ParserConfigurationException, JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        SoapMessage soapMessage = getSoapMessageForDom(doc);
        setSignatureAlgorithmInInterceptor.handleMessage(soapMessage);
        Assert.assertEquals(soapMessage.get(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM), SPConstants.SHA256);
    }

}
