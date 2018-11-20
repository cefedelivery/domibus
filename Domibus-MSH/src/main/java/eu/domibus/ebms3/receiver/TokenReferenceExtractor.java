package eu.domibus.ebms3.receiver;

import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.WSConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import static eu.domibus.ebms3.receiver.TrustSenderInterceptor.KEYINFO;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Inspect soap message security header to find the proper token reference method.
 * http://docs.oasis-open.org/wss-m/wss/v1.1.1/os/wss-x509TokenProfile-v1.1.1-os.html#_Toc307416641
 */
@Component
public class TokenReferenceExtractor {

    private static final Logger LOG = LoggerFactory.getLogger(TokenReferenceExtractor.class);
    public static final String REFERENCE = "Reference";
    public static final String URI = "URI";
    public static final String VALUE_TYPE = "ValueType";

    private static XPathFactory xpathFactory;
    private static XPath xPath;

    static {
        xpathFactory = XPathFactory.newInstance();
        xPath = xpathFactory.newXPath();
    }


    public TokenReference extractTokenReference(Element securityHeader) throws WSSecurityException {
        final Element secTokenRef = getSecTokenRef(securityHeader);

        final NodeList childNodes = secTokenRef.getChildNodes();
        LOG.debug("Security token reference content");
        for(int i=0;i<childNodes.getLength();i++){
            final Node item = childNodes.item(i);
            final String itemLocalName = item.getLocalName();
            LOG.debug("Child name:[{}]", itemLocalName);
            if(REFERENCE.equalsIgnoreCase(itemLocalName)){
                final NamedNodeMap attributes = item.getAttributes();
                final Node uri = attributes.getNamedItem(URI);
                final Node valueType = attributes.getNamedItem(VALUE_TYPE);
                if(uri==null || valueType==null){
                    return null;
                }
                final String uriValue = uri.getNodeValue();
                final String valueTypeValue = valueType.getNodeValue();
                LOG.debug("Binary security token URI:[{}], ValueType:[{}] ", uriValue, valueTypeValue);
                return new BinarySecurityTokenReference(valueTypeValue,uriValue);
            }
        }
        return null;
    }

    public Element getSecTokenRef(Element soapSecurityHeader) throws WSSecurityException {

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
