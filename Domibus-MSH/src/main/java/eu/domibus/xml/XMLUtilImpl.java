package eu.domibus.xml;

import eu.domibus.api.util.xml.DefaultUnmarshallerResult;
import eu.domibus.api.util.xml.UnmarshallerResult;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.validators.XmlValidationEventHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;

/**
 * Created by Cosmin Baciu on 14-Sep-16.
 */
@Component
public class XMLUtilImpl implements XMLUtil {

    @Override
    public UnmarshallerResult unmarshal(boolean ignoreWhitespaces, JAXBContext jaxbContext, InputStream xmlStream, InputStream xsdStream) throws SAXException, JAXBException, ParserConfigurationException, XMLStreamException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        if(xsdStream != null) {
            Schema schema = schemaFactory.newSchema(new StreamSource(xsdStream));
            unmarshaller.setSchema(schema);
        }

        XmlValidationEventHandler jaxbValidationEventHandler = new XmlValidationEventHandler();
        unmarshaller.setEventHandler(jaxbValidationEventHandler);

        SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setNamespaceAware(true);
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(xmlStream);
        if (ignoreWhitespaces) {
            eventReader = inputFactory.createFilteredReader(eventReader, new WhitespaceFilter());
        }
        DefaultUnmarshallerResult result = new DefaultUnmarshallerResult();
        result.setResult(unmarshaller.unmarshal(eventReader));
        result.setValid(!jaxbValidationEventHandler.hasErrors());
        result.setErrors(jaxbValidationEventHandler.getErrors());
        return result;
    }


}
