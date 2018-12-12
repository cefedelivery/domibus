package eu.domibus.ebms3.receiver;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.ebms3.SoapInterceptorTest;
import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.PKIUtil;
import eu.domibus.spring.SpringContextProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsOperations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static eu.domibus.ebms3.receiver.TrustSenderInterceptor.DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING;

/**
 * @author idragusa
 * @since 4.0
 */
@RunWith(JMockit.class)
public class TrustSenderInterceptorTest extends SoapInterceptorTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TrustSenderInterceptorTest.class);

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/receiver/";

    private static final String X_509_V_3 = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3";

    @Injectable
    CertificateService certificateService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected JAXBContext jaxbContextEBMS;

    @Injectable
    TokenReferenceExtractor tokenReferenceExtractor;

    @Tested
    TrustSenderInterceptor trustSenderInterceptor;

    @Bean
    @Qualifier("jmsTemplateCommand")
    public JmsOperations jmsOperations() {
        return Mockito.mock(JmsOperations.class);
    }

    PKIUtil pkiUtil = new PKIUtil();

    @Test
    public void testHandleMessageBinaryToken(@Mocked SpringContextProvider springContextProvider,@Mocked final Element securityHeader,@Mocked final BinarySecurityTokenReference binarySecurityTokenReference) throws XMLStreamException, ParserConfigurationException, JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException, WSSecurityException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        String trustoreFilename = RESOURCE_PATH + "nonEmptySource.jks";
        String trustorePassword = "1234";

        new Expectations(){{
            tokenReferenceExtractor.extractTokenReference(withAny(securityHeader));
            result=binarySecurityTokenReference;
            binarySecurityTokenReference.getUri();
            result="#X509-99bde7b7-932f-4dbd-82dd-3539ba51791b";
            binarySecurityTokenReference.getValueType();
            result= X_509_V_3;
        }};
        testHandleMessage(doc, trustoreFilename, trustorePassword);
    }

    @Test(expected = org.apache.cxf.interceptor.Fault.class)
    public void testSenderTrustFault(@Mocked SpringContextProvider springContextProvider,@Mocked final Element securityHeader,@Mocked final BinarySecurityTokenReference binarySecurityTokenReference) throws XMLStreamException, ParserConfigurationException, JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException, WSSecurityException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        String trustoreFilename = RESOURCE_PATH + "nonEmptySource.jks";
        String trustorePassword = "1234";

        new Expectations() {{
            tokenReferenceExtractor.extractTokenReference(withAny(securityHeader));
            result=binarySecurityTokenReference;
            binarySecurityTokenReference.getUri();
            result="#X509-99bde7b7-932f-4dbd-82dd-3539ba51791b";
            binarySecurityTokenReference.getValueType();
            result=X_509_V_3;
            certificateService.isCertificateValid((X509Certificate) any);
            result = false;
            domibusPropertyProvider.getBooleanDomainProperty(TrustSenderInterceptor.DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING);
            result = true;
        }};
        testHandleMessage(doc, trustoreFilename, trustorePassword);
    }

    @Test
    public void testSenderTrustNoSenderVerification(@Mocked SpringContextProvider springContextProvider,@Mocked final Element securityHeader,@Mocked BinarySecurityTokenReference binarySecurityTokenReference) throws XMLStreamException, ParserConfigurationException, JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException, WSSecurityException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        String trustoreFilename = RESOURCE_PATH + "nonEmptySource.jks";
        String trustorePassword = "1234";

        new Expectations() {{
            tokenReferenceExtractor.extractTokenReference(withAny(securityHeader));
            result=binarySecurityTokenReference;
            binarySecurityTokenReference.getUri();
            result="#X509-99bde7b7-932f-4dbd-82dd-3539ba51791b";
            binarySecurityTokenReference.getValueType();
            result=X_509_V_3;
            domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING);
            result = false;
        }};
        testHandleMessage(doc, trustoreFilename, trustorePassword);
        new Verifications() {{
            certificateService.isCertificateValid((X509Certificate) any);
            times = 0;
        }};
    }

    @Test
    public void testGetCertificateFromBinarySecurityTokenX509v3(@Mocked final BinarySecurityTokenReference binarySecurityTokenReference) throws XMLStreamException, ParserConfigurationException, WSSecurityException, CertificateException, URISyntaxException {
        new Expectations(){{
            binarySecurityTokenReference.getUri();
            result="#X509-9973d6a2-7819-4de2-a3d2-1bbdb2506df8";
            binarySecurityTokenReference.getValueType();
            result=X_509_V_3;
        }};
        Document doc = readDocument("dataset/as4/RawXMLMessageWithSpaces.xml");
        X509Certificate xc = trustSenderInterceptor.getCertificateFromBinarySecurityToken(doc.getDocumentElement(),binarySecurityTokenReference);
        Assert.assertNotNull(xc);
        Assert.assertNotNull(xc.getIssuerDN());
    }

    @Test
    public void testGetCertificateFromBinarySecurityTokenX509PKIPathv1(@Mocked final BinarySecurityTokenReference binarySecurityTokenReference) throws XMLStreamException, ParserConfigurationException, WSSecurityException, CertificateException, URISyntaxException {
        new Expectations(){{
            binarySecurityTokenReference.getUri();
            result="#X509-9973d6a2-7819-4de2-a3d2-1bbdb2506df8";
            binarySecurityTokenReference.getValueType();
            result="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509PKIPathv1";
        }};
        Document doc = readDocument("dataset/as4/RawXMLMessageWithSpacesAndPkiPath.xml");
        X509Certificate xc = trustSenderInterceptor.getCertificateFromBinarySecurityToken(doc.getDocumentElement(),binarySecurityTokenReference);
        Assert.assertNotNull(xc);
        Assert.assertNotNull(xc.getIssuerDN());
    }



    protected void testHandleMessage(Document doc, String trustoreFilename,  String trustorePassword) throws JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException {
        SoapMessage soapMessage = getSoapMessageForDom(doc);

        new Expectations() {{
            domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING);
            result = true;
        }};
        trustSenderInterceptor.handleMessage(soapMessage);
    }

    @Test
    public void testCheckCertificateValidityEnabled() throws Exception {
        final X509Certificate certificate = pkiUtil.createCertificate(BigInteger.ONE, null);
        final X509Certificate expiredCertificate = pkiUtil.createCertificate(BigInteger.ONE, new DateTime().minusDays(2).toDate(), new DateTime().minusDays(1).toDate(), null);

        new Expectations() {{
            domibusPropertyProvider.getBooleanDomainProperty(TrustSenderInterceptor.DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING);
            result = true;
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
            domibusPropertyProvider.getBooleanDomainProperty(TrustSenderInterceptor.DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING);
            result = false;
        }};
        Assert.assertTrue(trustSenderInterceptor.checkCertificateValidity(expiredCertificate, "test sender", false));
    }

    @Test
    public void testCheckSenderPartyTrust() throws Exception {
        final X509Certificate certificate = pkiUtil.createCertificate(BigInteger.ONE, null);

        new Expectations() {{
            domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING);
            result = true;
        }};

        Assert.assertTrue(trustSenderInterceptor.checkSenderPartyTrust(certificate, "GlobalSign", "messageID123", false));
        Assert.assertFalse(trustSenderInterceptor.checkSenderPartyTrust(certificate, "test sender", "messageID123", false));
    }

    @Test
    public void testCheckSenderPartyTrustDisabled() throws Exception {
        final X509Certificate certificate = pkiUtil.createCertificate(BigInteger.ONE, null);

        new Expectations() {{
            domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING);
            result = false;
        }};

        Assert.assertTrue(trustSenderInterceptor.checkSenderPartyTrust(certificate, "test sender", "messageID123", false));
    }

    @Test
    public void testHandleOneTestActivated(@Mocked final SoapMessage message){
        new Expectations(){{
            domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING);
            result=false;
            domibusPropertyProvider.getBooleanDomainProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING);
            result=false;
        }};
        trustSenderInterceptor.handleMessage(message);
        new Verifications(){{
           message.getExchange();times=0;
        }};
    }
}