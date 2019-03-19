package eu.domibus.util;

import com.google.common.io.CharStreams;
import eu.domibus.ebms3.common.model.ObjectFactory;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.MessageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author idragusa
 * @author Cosmin Baciu
 * @since 3.2.5
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class SoapUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SoapUtil.class);

    @Autowired
    protected TransformerFactory transformerFactory;

    public void logMessage(SOAPMessage request) throws IOException, TransformerException {
        if (LOG.isDebugEnabled()) {
            try (StringWriter sw = new StringWriter()) {
                transformerFactory.newTransformer().transform(new DOMSource(request.getSOAPPart()), new StreamResult(sw));

                LOG.debug(sw.toString());
                LOG.debug("received attachments:");
                final Iterator i = request.getAttachments();
                while (i.hasNext()) {
                    LOG.debug("attachment: {}", i.next());
                }
            }
        }
    }

    /**
     * Creates a SOAPMessage based on a CXF MessageImpl instance
     *
     * @param messageImpl
     * @return
     * @throws SOAPException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws TransformerException
     */
    public SOAPMessage createUserMessage(MessageImpl messageImpl) throws SOAPException, IOException, ParserConfigurationException, SAXException, TransformerException {
        LOG.debug("Creating SOAPMessage");
        SOAPMessage message = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage();

        final Collection<Attachment> attachments = messageImpl.getAttachments();
        for (Attachment attachment : attachments) {
            LOG.debug("Creating attachmentPart [{}]", attachment.getId());
            final AttachmentPart attachmentPart = message.createAttachmentPart(attachment.getDataHandler());

            attachmentPart.setContentId(attachment.getId());
            attachmentPart.setContentType(attachment.getDataHandler().getContentType());
            message.addAttachmentPart(attachmentPart);
            LOG.debug("Finished creating attachmentPart [{}]", attachment.getId());
        }

        LOG.debug("Getting soapEnvelopeString");
        final String soapEnvelopeString = IOUtils.toString(messageImpl.getContent(InputStream.class), StandardCharsets.UTF_8);
        LOG.debug("Finished getting soapEnvelopeString");

        final SOAPMessage soapMessage = createSOAPMessage(soapEnvelopeString);
        final SOAPElement next = (SOAPElement) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        message.getSOAPHeader().addChildElement(next);

        message.saveChanges();

        if (LOG.isDebugEnabled()) {
            final String rawXMLMessage = getRawXMLMessage(message);
            LOG.debug("Created SOAPMessage [{}]", rawXMLMessage);
        }

        return message;
    }

    public String getRawXMLMessage(SOAPMessage soapMessage) throws TransformerException {
        final StringWriter rawXmlMessageWriter = new StringWriter();

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.newTransformer().transform(
                new DOMSource(soapMessage.getSOAPPart()),
                new StreamResult(rawXmlMessageWriter));

        return rawXmlMessageWriter.toString();
    }

    public SOAPMessage createSOAPMessage(final String rawXml) throws SOAPException, IOException, ParserConfigurationException, SAXException {
        LOG.debug("Creating SOAPMessage from rawXML [{}]", rawXml);

        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage message = factory.createMessage();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();

        try (StringReader stringReader = new StringReader(rawXml); InputStream targetStream =
                new ByteArrayInputStream(CharStreams.toString(stringReader)
                        .getBytes(StandardCharsets.UTF_8.name()))) {

            LOG.debug("Parsing SOAPMessage document");
            Document document = builder.parse(targetStream);
            LOG.debug("Finished parsing SOAPMessage document");

            DOMSource domSource = new DOMSource(document);
            SOAPPart soapPart = message.getSOAPPart();
            soapPart.setContent(domSource);
            return message;
        }
    }


}
