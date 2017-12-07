package eu.domibus.ebms3.sender;

import eu.domibus.common.services.MessageExchangeService;
import eu.domibus.ebms3.SoapInterceptorTest;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.ebms3.receiver.PropertyValueExchangeInterceptor;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.spring.SpringContextProvider;
import junit.framework.Assert;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapMessage;
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
public class SaveRawPulledMessageInterceptorTest extends SoapInterceptorTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SaveRawPulledMessageInterceptorTest.class);

    @Injectable
    MessageExchangeService messageExchangeService;

    @Tested
    SaveRawPulledMessageInterceptor saveRawPulledMessageInterceptor;

    @Test
    public void testHandleMessage(@Mocked SpringContextProvider springContextProvider) throws XMLStreamException, ParserConfigurationException, JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        SoapMessage soapMessage = getSoapMessageForDom(doc);
        saveRawPulledMessageInterceptor.handleMessage(soapMessage);
        Assert.assertEquals(soapMessage.get(MSHDispatcher.MESSAGE_TYPE_OUT), MessageType.USER_MESSAGE);
    }


}
