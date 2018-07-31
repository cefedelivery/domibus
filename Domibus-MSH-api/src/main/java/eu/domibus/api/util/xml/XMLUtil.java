package eu.domibus.api.util.xml;

import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * Created by Cosmin Baciu on 14-Sep-16.
 */
public interface XMLUtil {

    UnmarshallerResult unmarshal(boolean ignoreWhitespaces, JAXBContext jaxbContext, InputStream xmlStream, InputStream xsdStream) throws SAXException, JAXBException, ParserConfigurationException, XMLStreamException;

    byte[] marshal(JAXBContext jaxbContext, Object input, InputStream xsdStream) throws SAXException, JAXBException, ParserConfigurationException, XMLStreamException;

}
