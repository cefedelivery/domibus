package eu.domibus.ebms3.receiver;

import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class TokenReferenceMethodExtractorTest {

   /* @Test
    public void testX509PKIPathv1TokenReference() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(this.getClass().getResourceAsStream("/bst_X509PKIPathv1_incoming_message.xml"));

        System.out.println(xmlDocument.getDocumentElement());

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();


        Node securityHeader = (Node) xPath.evaluate("/Envelope/Header/Security", xmlDocument, XPathConstants.NODE);
        System.out.println("Security header "+securityHeader);

        final TokenReferenceExtractor tokenReferenceExtractor = new TokenReferenceExtractor();
        final BinarySecurityTokenReference tokenReference = (BinarySecurityTokenReference) tokenReferenceExtractor.extractTokenReference(securityHeader);

    }

    @Test
    public void testx509v3TokenReference() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(this.getClass().getResourceAsStream("/bst_x509v3_incoming_message.xml"));

        System.out.println(xmlDocument.getDocumentElement());

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();


        Node securityHeader = (Node) xPath.evaluate("/Envelope/Header/Security", xmlDocument, XPathConstants.NODE);
        System.out.println("Security header "+securityHeader);

        final TokenReferenceExtractor tokenReferenceExtractor = new TokenReferenceExtractor();
        final BinarySecurityTokenReference tokenReference = (BinarySecurityTokenReference) tokenReferenceExtractor.extractTokenReference(securityHeader);

    }

    @Test
    public void testSubjectKeyIdentifierTokenReference() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(this.getClass().getResourceAsStream("/ski_incoming_message.xml"));

        System.out.println(xmlDocument.getDocumentElement());

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();


        Node securityHeader = (Node) xPath.evaluate("/Envelope/Header/Security", xmlDocument, XPathConstants.NODE);
        System.out.println("Security header "+securityHeader);

        final TokenReferenceExtractor tokenReferenceExtractor = new TokenReferenceExtractor();
        final KeyIdentifierTokenReference tokenReference = (KeyIdentifierTokenReference) tokenReferenceExtractor.extractTokenReference(securityHeader);

    }

    @Test
    public void testIssuerSerialTokenReference() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(this.getClass().getResourceAsStream("/is_incoming_message.xml"));

        System.out.println(xmlDocument.getDocumentElement());

        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();


        Node securityHeader = (Node) xPath.evaluate("/Envelope/Header/Security", xmlDocument, XPathConstants.NODE);
        System.out.println("Security header "+securityHeader);

        final TokenReferenceExtractor tokenReferenceExtractor = new TokenReferenceExtractor();
        final IssuerSerialTokenReference tokenReference = (IssuerSerialTokenReference) tokenReferenceExtractor.extractTokenReference(securityHeader);

    }*/

}