package eu.domibus.xml;

import eu.domibus.api.util.xml.DefaultUnmarshallerResult;
import eu.domibus.api.util.xml.UnmarshallerResult;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.validators.XmlValidationEventHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * StAX marshaller and unmarshaller utility class.
 *
 * @author Cosmin BACIU
 * @author Sebastian-Ion TINCU
 * @since 3.2
 */
@Component
public class XMLUtilImpl implements XMLUtil {

    @Override
    public UnmarshallerResult unmarshal(boolean ignoreWhitespaces, JAXBContext jaxbContext, InputStream xmlStream, InputStream xsdStream) throws SAXException, JAXBException, XMLStreamException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        if(xsdStream != null) {
            Schema schema = getSchema(xsdStream);
            unmarshaller.setSchema(schema);
        }

        XmlValidationEventHandler jaxbValidationEventHandler = new XmlValidationEventHandler();
        unmarshaller.setEventHandler(jaxbValidationEventHandler);

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

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

    @Override
    public byte[] marshal(JAXBContext jaxbContext, Object input, InputStream xsdStream) throws SAXException, JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        if (xsdStream != null) {
            Schema schema = getSchema(xsdStream);
            marshaller.setSchema(schema);
        }

        ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();
        marshaller.marshal(input, xmlStream);
        return xmlStream.toByteArray();
    }

    private Schema getSchema(InputStream xsdStream) throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, StringUtils.EMPTY);
        return schemaFactory.newSchema(new StreamSource(xsdStream));
    }

}
