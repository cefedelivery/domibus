package eu.domibus.common.xmladapter;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated  since 4.0. It will be removed in the future releases
 *
 * This adapter takes a List<String> containing a single element and maps it to the single String Node and vice versa.
 *
 * @author Christian Koch, Stefan Mueller
 */
@Deprecated
public class ToStringAdapter extends XmlAdapter<Node, List<String>> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ToStringAdapter.class);

    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    @Override
    /**
     * {@inheritDoc}
     */
    public List<String> unmarshal(final Node v) throws Exception {
        if (v != null) {
            final List<String> result = new ArrayList<>(1);
            result.add(this.nodeToString(v));

            return result;
        }

        return null;
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public Node marshal(final List<String> v) throws Exception {
        if (v.size() > 1) {
            throw new IllegalArgumentException("More than one Element in List");
        }

        return this.stringToNode(v.get(0));
    }

    private String nodeToString(final Node node) throws TransformerException {
        final StringWriter sw = new StringWriter();
        final Transformer t = this.transformerFactory.newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }

    private Node stringToNode(final String content) {
        try {
            final Document doc = this.documentBuilderFactory.newDocumentBuilder().parse(new InputSource(new StringReader(content)));

            if (doc.getChildNodes().getLength() == 1) {
                return doc.getChildNodes().item(1);
            }

        } catch (SAXException | IOException | ParserConfigurationException e) {
            ToStringAdapter.LOG.warn("Error during transformation of String to Node", e);
        }

        return null;
    }

}
