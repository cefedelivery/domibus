package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.SoapInterceptorTest;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.PKIUtil;
import eu.domibus.spring.SpringContextProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapMessage;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsOperations;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author idragusa
 * @since 4.0
 */
@RunWith(JMockit.class)
public class TrustSenderInterceptorTest extends SoapInterceptorTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TrustSenderInterceptorTest.class);

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/receiver/";

    @Injectable
    CertificateService certificateService;

    @Injectable
    protected JAXBContext jaxbContextEBMS;

    @Tested
    TrustSenderInterceptor trustSenderInterceptor;

    @Bean
    @Qualifier("jmsTemplateCommand")
    public JmsOperations jmsOperations() throws JAXBException {
        return Mockito.mock(JmsOperations.class);
    }

    PKIUtil pkiUtil = new PKIUtil();

    @Test
    public void testHandleMessageBinaryToken(@Mocked SpringContextProvider springContextProvider) throws XMLStreamException, ParserConfigurationException, JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        String trustoreFilename = RESOURCE_PATH + "nonEmptySource.jks";
        String trustorePassword = "1234";

        testHandleMessage(doc, trustoreFilename, trustorePassword);
    }

    @Test(expected = org.apache.cxf.interceptor.Fault.class)
    public void testSenderTrustFault(@Mocked SpringContextProvider springContextProvider) throws XMLStreamException, ParserConfigurationException, JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        String trustoreFilename = RESOURCE_PATH + "nonEmptySource.jks";
        String trustorePassword = "1234";

        new Expectations() {{
            certificateService.isCertificateValid((X509Certificate) any);
            result = false;
            domibusProperties.getProperty(TrustSenderInterceptor.DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING, "true");
            result = true;
        }};
        testHandleMessage(doc, trustoreFilename, trustorePassword);
    }

    @Test
    public void testSenderTrustNoSenderVerification(@Mocked SpringContextProvider springContextProvider) throws XMLStreamException, ParserConfigurationException, JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        String trustoreFilename = RESOURCE_PATH + "nonEmptySource.jks";
        String trustorePassword = "1234";

        new Expectations() {{
            domibusProperties.getProperty(TrustSenderInterceptor.DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING, "false");
            result = false;
        }};
        testHandleMessage(doc, trustoreFilename, trustorePassword);
        new Verifications() {{
            certificateService.isCertificateValid((X509Certificate) any);
            times = 0;
        }};
    }

    protected void testHandleMessage(Document doc, String trustoreFilename, String trustorePassword) throws JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException {
        SoapMessage soapMessage = getSoapMessageForDom(doc);

        new Expectations(trustSenderInterceptor) {{
            domibusProperties.getProperty(TrustSenderInterceptor.DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING, "false");
            result = true;
        }};
        trustSenderInterceptor.handleMessage(soapMessage);
    }

    @Test
    public void testCheckCertificateValidityEnabled() throws Exception {
        final X509Certificate certificate = pkiUtil.createCertificate(BigInteger.ONE, null);
        final X509Certificate expiredCertificate = pkiUtil.createCertificate(BigInteger.ONE, new DateTime().minusDays(2).toDate(), new DateTime().minusDays(1).toDate(), null);

        new Expectations() {{
            domibusProperties.getProperty(TrustSenderInterceptor.DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING, "true");
            result = "true";
            certificateService.isCertificateValid(certificate);
            result = true;
            certificateService.isCertificateValid(expiredCertificate);
            result = false;

        }};

        Assert.assertTrue(trustSenderInterceptor.checkCertificateValidity(certificate, "test sender", false));
        Assert.assertFalse(trustSenderInterceptor.checkCertificateValidity(expiredCertificate, "test sender", false));
    }

    @Test
    public void testCheckCertificateValidityDisabled() throws Exception {
        final X509Certificate expiredCertificate = pkiUtil.createCertificate(BigInteger.ONE, new DateTime().minusDays(2).toDate(), new DateTime().minusDays(1).toDate(), null);

        new Expectations() {{
            domibusProperties.getProperty(TrustSenderInterceptor.DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING, "true");
            result = "false";
        }};
        Assert.assertTrue(trustSenderInterceptor.checkCertificateValidity(expiredCertificate, "test sender", false));
    }

    @Test
    public void testCheckSenderPartyTrust() throws Exception {
        final X509Certificate certificate = pkiUtil.createCertificate(BigInteger.ONE, null);

        new Expectations() {{
            domibusProperties.getProperty(TrustSenderInterceptor.DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING, "false");
            result = "true";
        }};

        Assert.assertTrue(trustSenderInterceptor.checkSenderPartyTrust(certificate, "GlobalSign", "messageID123", false));
        Assert.assertFalse(trustSenderInterceptor.checkSenderPartyTrust(certificate, "test sender", "messageID123", false));
    }

    @Test
    public void testCheckSenderPartyTrustDisabled() throws Exception {
        final X509Certificate certificate = pkiUtil.createCertificate(BigInteger.ONE, null);

        new Expectations() {{
            domibusProperties.getProperty(TrustSenderInterceptor.DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING, "false");
            result = "false";
        }};

        Assert.assertTrue(trustSenderInterceptor.checkSenderPartyTrust(certificate, "test sender", "messageID123", false));
    }
}