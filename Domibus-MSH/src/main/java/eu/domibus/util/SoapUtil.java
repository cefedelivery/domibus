package eu.domibus.util;

import com.google.common.io.CharStreams;
import org.apache.commons.io.Charsets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * @author idragusa
 * @since 3.2.5
 */
public class SoapUtil {
    private static final Log logger = LogFactory.getLog(SoapUtil.class);

    public static String getRawXMLMessage(SOAPMessage soapMessage) throws TransformerException {
        String rawXMLMessage = null;
        final StringWriter sw = new StringWriter();

        TransformerFactory.newInstance().newTransformer().transform(
                new DOMSource(soapMessage.getSOAPPart()),
                new StreamResult(sw));

        rawXMLMessage = sw.toString();
        return rawXMLMessage;
    }

    public static SOAPMessage createSOAPMessage(final String rawXml) throws SOAPException, IOException, ParserConfigurationException, SAXException {
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();

        try(StringReader stringReader = new StringReader(rawXml); InputStream targetStream =
                new ByteArrayInputStream(CharStreams.toString(stringReader)
                        .getBytes(Charsets.UTF_8.name()))){
            Document document = builder.parse(targetStream);
            DOMSource domSource = new DOMSource(document);
            SOAPPart soapPart = message.getSOAPPart();
            soapPart.setContent(domSource);
            return message;
        }
    }






    }
