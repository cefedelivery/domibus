package eu.domibus.ebms3.receiver;

import com.sun.org.apache.xerces.internal.dom.TextImpl;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.ws.security.wss4j.CXFRequestData;
import org.apache.cxf.ws.security.wss4j.StaxSerializer;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.WSDocInfo;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.apache.wss4j.dom.engine.WSSecurityEngine;
import org.apache.wss4j.dom.str.EncryptedKeySTRParser;
import org.apache.wss4j.dom.str.STRParserParameters;
import org.apache.wss4j.dom.str.STRParserResult;
import org.apache.wss4j.dom.util.WSSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

/**
 * This interceptor is responsible of the trust of an incoming messages.
 * Useful info on this topic are here: http://tldp.org/HOWTO/SSL-Certificates-HOWTO/x64.html
 *
 * @author Martini Federico
 * @since 3.3
 */
public class TrustSenderInterceptor extends WSS4JInInterceptor {

    private static final String DOMIBUS_SENDERPARTY_TRUST_VERIFICATION = "domibus.senderparty.trust.verification";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TrustSenderInterceptor.class);

    public static final QName KEYINFO = new QName("http://www.w3.org/2000/09/xmldsig#", "KeyInfo");

    private Properties securityEncryptionProp;

    @Autowired
    @Qualifier("domibusProperties")
    private java.util.Properties domibusProperties;

    public TrustSenderInterceptor() {
        super(false);
    }


    public void setSecurityEncryptionProp(Properties securityEncryptionProp) {
        this.securityEncryptionProp = securityEncryptionProp;
    }

    /**
     * Intercepts a message to verify that the sender is trusted.
     *
     * @param message the incoming CXF soap message to handle
     */
    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        String msgId = (String) message.getExchange().get(MessageInfo.MESSAGE_ID_CONTEXT_PROPERTY);
        if (!isMessageSecured(message)) {
            LOG.info("Message [" + msgId + "] does not contain security info ==> skipping sender trust verification.");
            return;
        }

        if (!Boolean.parseBoolean(domibusProperties.getProperty(DOMIBUS_SENDERPARTY_TRUST_VERIFICATION, "false"))) {
            LOG.debug("Sender alias verification is disabled");
            return;
        }
        try {
            LOG.debug("Verifying sender trust for message [" + msgId + "]");
            String senderPartyName = getSenderPartyName(message);
            X509Certificate certificate = getSenderCertificate(message);
            if(certificate != null && org.apache.commons.lang.StringUtils.containsIgnoreCase(certificate.getSubjectDN().getName(), senderPartyName) ) {
                LOG.info("Sender [" + senderPartyName + "] is trusted for message [" + msgId + "]");
                return;
            }
            LOG.error("Sender [" + senderPartyName + "] is not trusted for message [" + msgId + "]. To disable this check, set the property " + DOMIBUS_SENDERPARTY_TRUST_VERIFICATION + " to false.");
            EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0101, "Sender [" + senderPartyName + "] is not trusted", msgId, null);
            ebMS3Ex.setMshRole(MSHRole.RECEIVING);
            throw ebMS3Ex;
        } catch (Exception ex) {
            LOG.error("Error while verifying parties trust", ex);
            throw new Fault(ex);
        }
    }

    private boolean isMessageSecured(SoapMessage msg) {
        try {
            return (getSecurityHeader(msg) == null) ? false : true;
        } catch (Exception ex) {
            LOG.error("Error while getting security info", ex);
            return false;
        }
    }

    private Element getSecurityHeader(SoapMessage msg) throws SOAPException, WSSecurityException {

        SOAPMessage doc = msg.getContent(SOAPMessage.class);
        return WSSecurityUtil.getSecurityHeader(doc.getSOAPHeader(), null, true);
    }

    private String getSenderPartyName(SoapMessage message) {
        String pmodeKey = (String) message.get(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY);
        List<String> contents = StringUtils.getParts(pmodeKey, ":");
        return contents.get(0);
    }

    private X509Certificate getSenderCertificate(SoapMessage msg)  {
        boolean utWithCallbacks = MessageUtils.getContextualBoolean(msg, "ws-security.validate.token", true);
        super.translateProperties(msg);
        CXFRequestData requestData = new CXFRequestData();
        WSSConfig config = (WSSConfig) msg.getContextualProperty(WSSConfig.class.getName());
        WSSecurityEngine engine;
        if (config != null) {
            engine = new WSSecurityEngine();
            engine.setWssConfig(config);
        } else {
            engine = super.getSecurityEngine(utWithCallbacks);
            if (engine == null) {
                engine = new WSSecurityEngine();
            }
            config = engine.getWssConfig();
        }

        requestData.setWssConfig(config);
        requestData.setEncryptionSerializer(new StaxSerializer());

        SoapVersion version = msg.getVersion();
        SAAJInInterceptor.INSTANCE.handleMessage(msg);
        try {
            requestData.setMsgContext(msg);
            decodeAlgorithmSuite(requestData);

            Crypto secCrypto = CryptoFactory.getInstance(securityEncryptionProp);
            requestData.setDecCrypto(secCrypto);

            // extract certificate from KeyInfo
            X509Certificate cert = getCertificateFromKeyInfo(requestData, getSecurityHeader(msg));

            if (cert == null) { // check for certificate embedded in the message under BinarySecurityToken tag
                LOG.info("Check for message embedded certificate in the BinarySecurityToken tag");
                cert = getCertificateFromBinarySecurityToken(getSecurityHeader(msg));
            }
            return cert;
        } catch (CertificateException certEx) {
            throw new SoapFault("CertificateException", certEx, version.getSender());
        } catch (WSSecurityException wssEx) {
            throw new SoapFault("WSSecurityException", wssEx, version.getSender());
        } catch (SOAPException soapEx) {
            throw new SoapFault("SOAPException", soapEx, version.getSender());
        }
    }

    private X509Certificate getCertificateFromBinarySecurityToken(Element securityHeader) throws WSSecurityException, CertificateException {
        Node binarySecurityTokenTag = securityHeader.getElementsByTagName("wsse:BinarySecurityToken").item(0).getFirstChild();
        if(binarySecurityTokenTag == null || !( binarySecurityTokenTag instanceof TextImpl) ) {
            return null;
        }

        String certStr = ( "-----BEGIN CERTIFICATE-----\n" + ((TextImpl)binarySecurityTokenTag).getData() + "\n-----END CERTIFICATE-----\n" );
        InputStream in = new ByteArrayInputStream(certStr.getBytes());
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)certFactory.generateCertificate(in);

        return cert;
    }


    private X509Certificate getCertificateFromKeyInfo(CXFRequestData data, Element securityHeader) throws WSSecurityException {

        X509Certificate[] certs;

        EncryptedKeySTRParser decryptedBytes;
        Element secTokenRef = getSecTokenRef(securityHeader);
        /* CXF class which has to be initialized in order to parse the Security token reference */
        STRParserParameters encryptedEphemeralKey1 = new STRParserParameters();
        encryptedEphemeralKey1.setData(data);
        encryptedEphemeralKey1.setWsDocInfo(new WSDocInfo(securityHeader.getOwnerDocument()));
        encryptedEphemeralKey1.setStrElement(secTokenRef);
        decryptedBytes = new EncryptedKeySTRParser();
        /* This Apache CXF call will look for a certificate in the Truststore whose Subject Key Identifier bytes matches the <wsse:SecurityTokenReference><wsse:KeyIdentifier> bytes */
        STRParserResult refList = decryptedBytes.parseSecurityTokenReference(encryptedEphemeralKey1);
        certs = refList.getCertificates();

        if (certs == null || certs.length < 1) {
            LOG.warn("No certificate found");
            return null;
        }
        return certs[0];
    }

    private static Element getSecTokenRef(Element soapSecurityHeader) throws WSSecurityException {

        for (Node currentChild = soapSecurityHeader.getFirstChild(); currentChild != null; currentChild = currentChild.getNextSibling()) {
            if (WSConstants.SIGNATURE.getLocalPart().equals(currentChild.getLocalName()) && WSConstants.SIGNATURE.getNamespaceURI().equals(currentChild.getNamespaceURI())) {
                Element signatureEl = (Element) currentChild;
                for (Node innerCurrentChild = signatureEl.getFirstChild(); innerCurrentChild != null; innerCurrentChild = innerCurrentChild.getNextSibling()) {
                    if (KEYINFO.getLocalPart().equals(innerCurrentChild.getLocalName()) && KEYINFO.getNamespaceURI().equals(innerCurrentChild.getNamespaceURI())) {
                        return (Element) innerCurrentChild.getFirstChild();
                    }
                }
            }
        }
        return null;
    }

}


