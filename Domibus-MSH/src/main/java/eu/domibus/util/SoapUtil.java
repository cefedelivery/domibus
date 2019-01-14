package eu.domibus.util;

import com.google.common.io.CharStreams;
import eu.domibus.ebms3.common.model.ObjectFactory;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.MessageImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Collection;

/**
 * @author idragusa
 * @author Cosmin Baciu
 * @since 3.2.5
 */
public class SoapUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SoapUtil.class);

    public static SOAPMessage createUserMessage(MessageImpl messageImpl) throws SOAPException, IOException, ParserConfigurationException, SAXException, TransformerException {
        LOG.debug("Creating SOAPMessage");
        SOAPMessage message = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();

        final Collection<Attachment> attachments = messageImpl.getAttachments();
        for (Attachment attachment : attachments) {
            final AttachmentPart attachmentPart = message.createAttachmentPart(attachment.getDataHandler());

            attachmentPart.setContentId(attachment.getId());
            attachmentPart.setContentType(attachment.getDataHandler().getContentType());//to check
            message.addAttachmentPart(attachmentPart);
        }

        final String soapEnvelopeString = IOUtils.toString(messageImpl.getContent(InputStream.class));
        final SOAPMessage soapMessage = SoapUtil.createSOAPMessage(soapEnvelopeString);
        final SOAPElement next = (SOAPElement) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        message.getSOAPHeader().addChildElement(next);

        message.saveChanges();

        if (LOG.isDebugEnabled()) {
            final String rawXMLMessage = SoapUtil.getRawXMLMessage(message);
            LOG.debug("Created SOAPMessage [{}]", rawXMLMessage);
        }

        return message;
    }

    public static String getRawXMLMessage(SOAPMessage soapMessage) throws TransformerException {
        final StringWriter rawXmlMessageWriter = new StringWriter();

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.newTransformer().transform(
                new DOMSource(soapMessage.getSOAPPart()),
                new StreamResult(rawXmlMessageWriter));

        return rawXmlMessageWriter.toString();
    }

    public static SOAPMessage createSOAPMessage(final String rawXml) throws SOAPException, IOException, ParserConfigurationException, SAXException {
        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();

        try (StringReader stringReader = new StringReader(rawXml); InputStream targetStream =
                new ByteArrayInputStream(CharStreams.toString(stringReader)
                        .getBytes(Charsets.UTF_8.name()))) {
            Document document = builder.parse(targetStream);
            DOMSource domSource = new DOMSource(document);
            SOAPPart soapPart = message.getSOAPPart();
            soapPart.setContent(domSource);
            return message;
        }
    }


}
