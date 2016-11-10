package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.ebms3.sender.MSHDispatcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;

/**
 * This interceptor is responsible of the trust of an incoming messages.
 *
 * @author Martini Federico
 * @since 3.3
 */
public class TrustSenderInterceptor extends WSS4JInInterceptor {

    private static final Log LOG = LogFactory.getLog(TrustSenderInterceptor.class);

    public static final QName KEYINFO = new QName("http://www.w3.org/2000/09/xmldsig#", "KeyInfo");

    private Properties securityEncryptionProp;

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
        LOG.debug("Verifying sender trust for message [" + msgId + "]");
        try {
            String pmodeKey = (String) message.get(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY);
            List<String> contents = StringUtils.getParts(pmodeKey, ":");
            String alias = contents.get(0);
            X509Certificate certificate = getSenderCertificate(message);
            if (certificate != null && certificate.getSubjectDN().getName().contains(alias)) {
                LOG.info("Sender [" + alias + "] is trusted for message [" + msgId + "]");
            } else {
                LOG.error("Sender [" + alias + "] is not trusted for message [" + msgId + "]");
                EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0101, msgId, "Sender [" + alias + "] is not trusted", null);
                ebMS3Ex.setMshRole(MSHRole.RECEIVING);
                throw ebMS3Ex;
            }
        } catch (Exception ex) {
            throw new Fault(ex);
        }
    }

    private X509Certificate getSenderCertificate(SoapMessage msg) throws Exception {

        boolean utWithCallbacks = MessageUtils.getContextualBoolean(msg, "ws-security.validate.token", true);
        this.translateProperties(msg);
        CXFRequestData requestData = new CXFRequestData();
        WSSConfig config = (WSSConfig) msg.getContextualProperty(WSSConfig.class.getName());
        WSSecurityEngine engine;
        if (config != null) {
            engine = new WSSecurityEngine();
            engine.setWssConfig(config);
        } else {
            engine = this.getSecurityEngine(utWithCallbacks);
            if (engine == null) {
                engine = new WSSecurityEngine();
            }
            config = engine.getWssConfig();
        }

        requestData.setWssConfig(config);
        requestData.setEncryptionSerializer(new StaxSerializer());

        SoapVersion version = msg.getVersion();
        SAAJInInterceptor.INSTANCE.handleMessage(msg);
        SOAPMessage doc = msg.getContent(SOAPMessage.class);
        try {
            requestData.setMsgContext(msg);
            decodeAlgorithmSuite(requestData);

            Crypto secCrypto = CryptoFactory.getInstance(securityEncryptionProp);
            requestData.setDecCrypto(secCrypto);

            Element secHeader = WSSecurityUtil.getSecurityHeader(doc.getSOAPHeader(), null, true);
            return getCertificateFromKeyInfo(requestData, secHeader);

        } catch (WSSecurityException wssEx) {
            throw new SoapFault("WSSecurityException", wssEx, version.getSender());
        } catch (SOAPException soapEx) {
            throw new SoapFault("SOAPException", soapEx, version.getSender());
        }
    }

    private X509Certificate getCertificateFromKeyInfo(CXFRequestData data, Element securityHeader) throws Exception {

        X509Certificate[] certs;

        EncryptedKeySTRParser decryptedBytes;
        Element secTokenRef = getSecTokenRef(securityHeader);
        STRParserParameters encryptedEphemeralKey1 = new STRParserParameters();
        encryptedEphemeralKey1.setData(data);
        encryptedEphemeralKey1.setWsDocInfo(new WSDocInfo(securityHeader.getOwnerDocument()));
        encryptedEphemeralKey1.setStrElement(secTokenRef);
        decryptedBytes = new EncryptedKeySTRParser();
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


