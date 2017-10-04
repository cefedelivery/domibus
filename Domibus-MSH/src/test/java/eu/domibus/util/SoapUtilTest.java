package eu.domibus.util;

import eu.domibus.ebms3.sender.DispatchClientDefaultProvider;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.wss4j.dom.WSConstants;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.io.IOException;

/**
 * @author idragusa
 * @since 3.2.5
 */
public class SoapUtilTest {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SoapUtilTest.class);

    @Test
    public void getRawXMLMessageTest() throws Exception {

        final String expectedRawMessage =  FileUtils.readFileToString(new File("target/test-classes/dataset/as4/RawXMLMessage.xml"));

        SOAPMessage soapMessage = SoapUtilTest.createSOAPMessage("SOAPMessage.xml");
        String rawXMLMessage = SoapUtil.getRawXMLMessage(soapMessage);
        Assert.assertEquals(expectedRawMessage, rawXMLMessage.replaceAll("\\s+",""));

    }

    public static SOAPMessage createSOAPMessage(String dataset) throws SOAPException, IOException, ParserConfigurationException, SAXException {

        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
        SOAPMessage message = factory.createMessage();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document document = builder.parse(SoapUtilTest.class.getClassLoader().getResourceAsStream("dataset/as4/" + dataset));
        DOMSource domSource = new DOMSource(document);
        SOAPPart soapPart = message.getSOAPPart();
        soapPart.setContent(domSource);

        AttachmentPart attachment = message.createAttachmentPart();
        attachment.setContent(Base64.decodeBase64("PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=".getBytes()), "text/xml");
        attachment.setContentId("sbdh-order");
        message.addAttachmentPart(attachment);

        message.setProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, "blue_gw:red_gw:testService1:tc1Action::pushTestcase1tc1Action");
        try {
            SOAPHeader soapHeader = message.getSOAPHeader();
        } catch (Exception e) {
            LOG.error("Could not get SOAPHeader", e);
        }
        return message;
    }

    @Test
    public void testCreateSOAPMessage(){
        try {
         //   SOAPMessage soapMessage = SoapUtilTest.createSOAPMessage("SOAPMessage.xml");
           // soapMessage.getSOAPHeader().getElementsByTagNameNS(WSConstants.SIG_NS, WSConstants.SIG_LN).item(0);
            SOAPMessage soapMessage = SoapUtil.createSOAPMessage("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"><env:Header><eb:Messaging xmlns:eb=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" env:mustUnderstand=\"true\" wsu:Id=\"_97542ab6-75be-4d73-a40b-621cfacf2107\"><eb:UserMessage mpc=\"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC\"><eb:MessageInfo><eb:Timestamp>2017-06-12T14:18:32.000Z</eb:Timestamp><eb:MessageId>0cee31a7-81b2-456b-a67c-63be12234fa4@domibus.eu</eb:MessageId></eb:MessageInfo><eb:PartyInfo><eb:From><eb:PartyId type=\"urn:oasis:names:tc:ebcore:partyid-type:unregistered\">domibus-blue</eb:PartyId><eb:Role>http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator</eb:Role></eb:From><eb:To><eb:PartyId type=\"urn:oasis:names:tc:ebcore:partyid-type:unregistered\">domibus-red</eb:PartyId><eb:Role>http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder</eb:Role></eb:To></eb:PartyInfo><eb:CollaborationInfo><eb:Service type=\"tc1\">bdx:pullProcess</eb:Service><eb:Action>TCPull</eb:Action><eb:ConversationId>7be1333a-e1c7-46ec-83b6-957e4780b43e@domibus.eu</eb:ConversationId></eb:CollaborationInfo><eb:MessageProperties><eb:Property name=\"finalRecipient\">urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4</eb:Property><eb:Property name=\"originalSender\">urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1</eb:Property></eb:MessageProperties><eb:PayloadInfo><eb:PartInfo href=\"cid:message\"><eb:PartProperties><eb:Property name=\"MimeType\">text/xml</eb:Property></eb:PartProperties></eb:PartInfo></eb:PayloadInfo></eb:UserMessage></eb:Messaging><wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" env:mustUnderstand=\"true\"><xenc:EncryptedKey xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\" Id=\"EK-16493c66-62f4-46d0-8993-4716256ebb6a\"><xenc:EncryptionMethod Algorithm=\"http://www.w3.org/2009/xmlenc11#rsa-oaep\"><ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"/><xenc11:MGF Algorithm=\"http://www.w3.org/2009/xmlenc11#mgf1sha256\" xmlns:xenc11=\"http://www.w3.org/2009/xmlenc11#\"/></xenc:EncryptionMethod><ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"><wsse:SecurityTokenReference><wsse:KeyIdentifier EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\" ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509SubjectKeyIdentifier\">+TFkYJxVcmfDaiJFTBH7aFecVWw=</wsse:KeyIdentifier></wsse:SecurityTokenReference></ds:KeyInfo><xenc:CipherData><xenc:CipherValue>kIh4jA17JzXrPitfX4AX5Rrk3rdS1MSs/yUDQs2mNN3JShVg+9riTFw7SBr28lWUIo7j7EmvWuCDKNF58hV/zfHqVj2soZCqAY9oe2m+mY7dilwybkIn+eAkWPSvryaR1cYbCI6RK5RVS5fxlfSyRwgOYGbqWlQOyurN+f4clxD4bYYTIW/nxkPNyYyjxZ+9Sy3sHT7rsyhVerL3xmOW/AqU145O9g8Xf5nqbL8zyeK02OOWoHxGc32E3eN13zJd2gSNvY/FOUbKT8de73fnDTjiSuoLDQFM+3i7ij9Op8Db5M2R+iB7lMWqFbq/5acXOmDK5pQ53baQfUYV6gFApA==</xenc:CipherValue></xenc:CipherData><xenc:ReferenceList><xenc:DataReference URI=\"#ED-dd2cdc3a-24af-44ba-ba2a-5983baeab47e\"/></xenc:ReferenceList></xenc:EncryptedKey><xenc:EncryptedData Id=\"ED-dd2cdc3a-24af-44ba-ba2a-5983baeab47e\" MimeType=\"text/xml\" Type=\"http://docs.oasis-open.org/wss/oasis-wss-SwAProfile-1.1#Attachment-Content-Only\" xmlns:xenc=\"http://www.w3.org/2001/04/xmlenc#\"><xenc:EncryptionMethod Algorithm=\"http://www.w3.org/2009/xmlenc11#aes128-gcm\"/><ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"><wsse:SecurityTokenReference xmlns:wsse11=\"http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd\" wsse11:TokenType=\"http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1#EncryptedKey\"><wsse:Reference URI=\"#EK-16493c66-62f4-46d0-8993-4716256ebb6a\"/></wsse:SecurityTokenReference></ds:KeyInfo><xenc:CipherData><xenc:CipherReference URI=\"cid:message\"><xenc:Transforms><ds:Transform Algorithm=\"http://docs.oasis-open.org/wss/oasis-wss-SwAProfile-1.1#Attachment-Ciphertext-Transform\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"/></xenc:Transforms></xenc:CipherReference></xenc:CipherData></xenc:EncryptedData><ds:Signature xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" Id=\"SIG-6c985bcc-a57b-43b0-88f8-7af2cbf4cd2c\"><ds:SignedInfo><ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"><ec:InclusiveNamespaces xmlns:ec=\"http://www.w3.org/2001/10/xml-exc-c14n#\" PrefixList=\"env\"/></ds:CanonicalizationMethod><ds:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/><ds:Reference URI=\"#_7294bcd7-5573-473a-a92d-4a922eb5f0a2\"><ds:Transforms><ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/></ds:Transforms><ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><ds:DigestValue>mYYzRwSWVEjtbO87APBCuw+UROZAl4PlGSpCBTegbxo=</ds:DigestValue></ds:Reference><ds:Reference URI=\"#_97542ab6-75be-4d73-a40b-621cfacf2107\"><ds:Transforms><ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/></ds:Transforms><ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><ds:DigestValue>7DJdcu3yLirTKKvJsNfGalEIUm9bgxbWerQfcKjW+Us=</ds:DigestValue></ds:Reference><ds:Reference URI=\"cid:message\"><ds:Transforms><ds:Transform Algorithm=\"http://docs.oasis-open.org/wss/oasis-wss-SwAProfile-1.1#Attachment-Content-Signature-Transform\"/></ds:Transforms><ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><ds:DigestValue>zdhcTJsbQ6qX5e8IkdlyJzBk9bBuTf8MyKqmyYXZzwY=</ds:DigestValue></ds:Reference></ds:SignedInfo><ds:SignatureValue>hc0erN3js6EeHrf5dUrvWANznHqYShfERr4BdyIy6s+LS6QEnYis0XXxHeDm7bc4eEG0CizHy11gUhM+SbabkwWWdwD44ztVk9rt/P0iZKdT6xu78G1N89G1MQVTekb3nKWfKUSjp6iw0yK7JnUZ0f5/q6gDqn+eXaxGDSxElZUvu9xAH/GKQjevyIeB+8CSn5TOdidwuxNkPGo+dluopRVZOnWUOTMxZIZg6dcYGz/Rf51eyBlBGXPliYJzJZXpsC36J/jUbfHtVXZNl2L5hWjIb6itVRpO4c1MXggw1lq9TKGZXOltVbmj+S+vNSq6p1eMcASXfwkHqoZGzoqb2w==</ds:SignatureValue><ds:KeyInfo Id=\"KI-0ba25b18-75a4-4f47-9347-687324083ad6\"><wsse:SecurityTokenReference wsu:Id=\"STR-05002518-caa1-436a-bb82-27bcb3c1f54a\"><wsse:KeyIdentifier EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\" ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509SubjectKeyIdentifier\">Ke0une7Yg61ZD17Msadkt4TrLtQ=</wsse:KeyIdentifier></wsse:SecurityTokenReference></ds:KeyInfo></ds:Signature></wsse:Security></env:Header><env:Body xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"_7294bcd7-5573-473a-a92d-4a922eb5f0a2\"/></env:Envelope>");
            soapMessage.getSOAPHeader().getElementsByTagNameNS(WSConstants.SIG_NS, WSConstants.SIG_LN).item(0);
        } catch (SOAPException|IOException|ParserConfigurationException|SAXException  e) {
            LOG.error(e.getMessage(), e);
            Assert.assertFalse(true);
        }
    }

}
