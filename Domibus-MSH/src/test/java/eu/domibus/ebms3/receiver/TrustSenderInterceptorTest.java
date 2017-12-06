package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.pki.PKIUtil;
import eu.domibus.spring.SpringContextProvider;
import eu.domibus.util.SoapUtil;
import eu.domibus.util.SoapUtilTest;
import eu.domibus.wss4j.common.crypto.CryptoService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.Bus;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyBuilderImpl;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.core.JmsOperations;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * @author idragusa
 * @since 3.3
 */
@RunWith(JMockit.class)
public class TrustSenderInterceptorTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TrustSenderInterceptorTest.class);

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/receiver/";

    private static final String SOURCE_TRUSTSTORE = "gateway_truststore.jks";

    @Injectable
    Properties domibusProperties;

    @Injectable
    CertificateService certificateService;

    @Injectable
    protected JAXBContext jaxbContextEBMS;

    @InjectMocks
    CryptoService cryptoService;

    @Tested
    TrustSenderInterceptor trustSenderInterceptor;

    PKIUtil pkiUtil = new PKIUtil();

    @Before
    public void init() {
        System.setProperty("javax.xml.soap.MessageFactory", "com.sun.xml.internal.messaging.saaj.soap.ver1_2.SOAPMessageFactory1_2Impl");
        System.setProperty("javax.xml.bind.JAXBContext", "com.sun.xml.internal.bind.v2.ContextFactory");
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Test
    public void testHandleMessage2(@Mocked SpringContextProvider springContextProvider) throws Exception {
        Document doc = readDocument("dataset/as4/SoapRequest.xml");
        SoapMessage soapMessage = getSoapMessageForDom(doc);
        Properties prop = new Properties();
        prop.setProperty("org.apache.ws.security.crypto.merlin.trustStore.password", "test123");
        prop.setProperty("org.apache.ws.security.crypto.merlin.load.cacerts", "false");
        prop.setProperty("org.apache.ws.security.crypto.provider", "eu.domibus.wss4j.common.crypto.Merlin");
        prop.setProperty("org.apache.ws.security.crypto.merlin.trustStore.file", RESOURCE_PATH + SOURCE_TRUSTSTORE);
        prop.setProperty("org.apache.ws.security.crypto.merlin.trustStore.type", "jks");
        trustSenderInterceptor.setSecurityEncryptionProp(prop);

        byte[] sourceKeyStore = FileUtils.readFileToByteArray(new File(RESOURCE_PATH + SOURCE_TRUSTSTORE));

        cryptoService = new CryptoService();
        cryptoService.setTrustStoreProperties(prop);
        cryptoService.replaceTruststore(sourceKeyStore, "test123");

        new Expectations() {{
            SpringContextProvider.getApplicationContext().getBean("cryptoService");
            result = cryptoService;
        }};
        trustSenderInterceptor.handleMessage(soapMessage);
    }

    @Test
    public void testHandleMessage3(@Mocked SpringContextProvider springContextProvider) throws Exception {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        SoapMessage soapMessage = getSoapMessageForDom(doc);
        Properties prop = new Properties();
        prop.setProperty("org.apache.ws.security.crypto.merlin.trustStore.password", "1234");
        prop.setProperty("org.apache.ws.security.crypto.merlin.load.cacerts", "false");
        prop.setProperty("org.apache.ws.security.crypto.provider", "eu.domibus.wss4j.common.crypto.Merlin");
        prop.setProperty("org.apache.ws.security.crypto.merlin.trustStore.file", RESOURCE_PATH + "nonEmptySource.jks");
        prop.setProperty("org.apache.ws.security.crypto.merlin.trustStore.type", "jks");
        trustSenderInterceptor.setSecurityEncryptionProp(prop);

        byte[] sourceKeyStore = FileUtils.readFileToByteArray(new File(RESOURCE_PATH + "nonEmptySource.jks"));

        cryptoService = new CryptoService();
        cryptoService.setTrustStoreProperties(prop);
        cryptoService.replaceTruststore(sourceKeyStore, "1234");

        new Expectations() {{
            SpringContextProvider.getApplicationContext().getBean("cryptoService");
            result = cryptoService;
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

    protected Document readDocument(String name) throws Exception,
            ParserConfigurationException {
        InputStream inStream = getClass().getClassLoader().getResourceAsStream(name);
        return StaxUtils.read(inStream);
    }

    protected SoapMessage getSoapMessageForDom(Document doc) throws SOAPException {

        SOAPMessage saajMsg = MessageFactory.newInstance().createMessage();
        SOAPPart part = saajMsg.getSOAPPart();
        part.setContent(new DOMSource(doc));
        saajMsg.saveChanges();

        // Hack to create the context map
        MessageImpl message = new MessageImpl();
        message.put(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, "blue_gw:red_gw:testService1:tc1Action::pushTestcase1tc1Action");
        SoapMessage msg = new SoapMessage(message);
        Exchange ex = new ExchangeImpl();
        ex.setInMessage(msg);
        msg.setContent(SOAPMessage.class, saajMsg);

        InterceptorChain ic = new PhaseInterceptorChain((new PhaseManagerImpl()).getOutPhases());
        msg.setInterceptorChain(ic);
        ExchangeImpl exchange = new ExchangeImpl();
        Bus bus = new ExtensionManagerBus();
        bus.setExtension(new PolicyBuilderImpl(bus), PolicyBuilder.class);
        exchange.put(Bus.class, bus);
        msg.setExchange(exchange);

        return msg;
    }

    @Test
    public void testHandleMessage() throws ParserConfigurationException, IOException, SAXException, SOAPException {
        InputStream is = SoapUtilTest.class.getClassLoader().getResourceAsStream("dataset/as4/SoapRequest.xml");
        String request = IOUtils.toString(is, StandardCharsets.UTF_8);
        LOG.info(request + "");
        SOAPMessage soapMessage = SoapUtil.createSOAPMessage(request);
        AttachmentPart attachment = soapMessage.createAttachmentPart();
        attachment.setContent(Base64.decodeBase64("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=".getBytes()), "text/xml");
        attachment.setContentId("sbdh-order");
        soapMessage.addAttachmentPart(attachment);

        soapMessage.setProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, "blue_gw:red_gw:testService1:tc1Action::pushTestcase1tc1Action");
        try {
            SOAPHeader soapHeader = soapMessage.getSOAPHeader();
        } catch (Exception e) {
            LOG.error("Could not get SOAPHeader", e);
        }

        SoapMessage sm = new SoapMessage(new MessageImpl());
        sm.setContent(SOAPMessage.class, soapMessage);
        InterceptorChain ic = new PhaseInterceptorChain((new PhaseManagerImpl()).getOutPhases());
        sm.setInterceptorChain(ic);
        ExchangeImpl exchange = new ExchangeImpl();
        Bus bus = new ExtensionManagerBus();
        bus.setExtension(new PolicyBuilderImpl(bus), PolicyBuilder.class);
        exchange.put(Bus.class, bus);
        sm.setExchange(exchange);
        //SoapMessage sm = createSoapMessage("SoapRequest.xml");
        trustSenderInterceptor.handleMessage(sm);
    }

}