package eu.domibus.ebms3.receiver;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.WSConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class TokenReferenceMethodExtractorTest {


    @Tested
    private TokenReferenceExtractor tokenReferenceExtractor;

    @Test
    public void testX509PKIPathv1TokenReference(@Mocked final Element securityHeader,
                                                @Mocked final Element signature,
                                                @Mocked final Element keyInfo,
                                                @Mocked final Element securityTokenrRefecence,
                                                @Mocked final Node uriNode,
                                                @Mocked final Node valueTypeNode) throws WSSecurityException {

        final String uri = "#X509-5f905b9f-f1e2-4f05-8369-123f330455d1";
        final String valueType = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509PKIPathv1";

        new Expectations() {{
            securityHeader.getFirstChild();
            result = signature;
            signature.getLocalName();
            result = WSConstants.SIGNATURE.getLocalPart();
            signature.getNamespaceURI();
            result = WSConstants.SIGNATURE.getNamespaceURI();
            signature.getFirstChild();
            result = keyInfo;
            keyInfo.getLocalName();
            result = TrustSenderInterceptor.KEYINFO.getLocalPart();
            keyInfo.getNamespaceURI();
            result = TrustSenderInterceptor.KEYINFO.getNamespaceURI();
            keyInfo.getFirstChild();
            result = securityTokenrRefecence;
            securityTokenrRefecence.getChildNodes().getLength();
            result = 1;
            securityTokenrRefecence.getChildNodes().item(0).getLocalName();
            result = TokenReferenceExtractor.REFERENCE;
            securityTokenrRefecence.getChildNodes().item(0).getAttributes().getNamedItem(TokenReferenceExtractor.URI);
            result = uriNode;
            uriNode.getNodeValue();
            result = uri;
            securityTokenrRefecence.getChildNodes().item(0).getAttributes().getNamedItem(TokenReferenceExtractor.VALUE_TYPE);
            result = valueTypeNode;
            valueTypeNode.getNodeValue();
            result = valueType;

        }};
        final BinarySecurityTokenReference tokenReference = (BinarySecurityTokenReference) tokenReferenceExtractor.extractTokenReference(securityHeader);
        Assert.assertEquals(uri, tokenReference.getUri());
        Assert.assertEquals(valueType, tokenReference.getValueType());

    }

    @Test
    public void testKeySubjectIdendtifier(@Mocked final Element securityHeader,
                                          @Mocked final Element signature,
                                          @Mocked final Element keyInfo,
                                          @Mocked final Element securityTokenrRefecence) throws WSSecurityException {

        final String uri = "#X509-5f905b9f-f1e2-4f05-8369-123f330455d1";
        final String valueType = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509PKIPathv1";

        new Expectations() {{
            securityHeader.getFirstChild();
            result = signature;
            signature.getLocalName();
            result = WSConstants.SIGNATURE.getLocalPart();
            signature.getNamespaceURI();
            result = WSConstants.SIGNATURE.getNamespaceURI();
            signature.getFirstChild();
            result = keyInfo;
            keyInfo.getLocalName();
            result = TrustSenderInterceptor.KEYINFO.getLocalPart();
            keyInfo.getNamespaceURI();
            result = TrustSenderInterceptor.KEYINFO.getNamespaceURI();
            keyInfo.getFirstChild();
            result = securityTokenrRefecence;
            securityTokenrRefecence.getChildNodes().getLength();
            result = 1;
            securityTokenrRefecence.getChildNodes().item(0).getLocalName();
            result = "KeyIdentifier";

        }};
        final BinarySecurityTokenReference tokenReference = (BinarySecurityTokenReference) tokenReferenceExtractor.extractTokenReference(securityHeader);
        Assert.assertNull(tokenReference);

    }

}