package eu.domibus.plugin.jms;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.transformer.OutgoingMessageTransformer;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class JMSOutgoingMessageTransformer implements OutgoingMessageTransformer {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSOutgoingMessageTransformer.class);

    @Autowired
    @Qualifier(value = "jmsJaxbContext")
    private JAXBContext jaxbContext;

    @Override
    public void transformOutgoingMessage(Submission submission, SOAPMessage message) {
        final List<String> samlAssertions = getSamlAssertions(submission);
        if (samlAssertions == null || samlAssertions.isEmpty()) {
            LOG.info("No SAML assertions found, the SOAPMessage will not be modified");
            return;
        }
        LOG.info("Found [{}] SAML assertions", samlAssertions.size());

        try {
            SOAPElement security = message.getSOAPHeader().addChildElement(WSConstants.WSSE_LN, "wsse", WSConstants.WSSE_NS);
            for (String samlAssertion : samlAssertions) {
                addSamlAssertionToSOAPMessage(samlAssertion, security);
            }
        } catch (Exception e) {
            LOG.error("Error adding SAML assertions", e);
        }
    }

    protected void addSamlAssertionToSOAPMessage(String samlAssertion, SOAPElement security) throws ParserConfigurationException, IOException, SAXException {
        LOG.info("Adding SAML assertion [{}] to the SOAPMessage", samlAssertion);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();


        /* parse existing file to DOM */
        Document document = documentBuilder.parse(new ByteArrayInputStream(samlAssertion.getBytes(StandardCharsets.UTF_8)));

        Document securityDoc = security.getOwnerDocument();
        Node newNode = securityDoc.importNode(document.getFirstChild(), true);

        //Add the Node
        security.appendChild(newNode);
    }


    protected List<String> getSamlAssertions(Submission submission) {
        List<String> result = new ArrayList<>();

        final Collection<Submission.TypedProperty> messageProperties = submission.getMessageProperties();
        for (Submission.TypedProperty property : messageProperties) {
            if (property.getKey().contains("saml")) {
                result.add(property.getValue());
            }
        }

        return result;
    }
}
