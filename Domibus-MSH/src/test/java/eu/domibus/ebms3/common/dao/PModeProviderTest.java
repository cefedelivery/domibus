package eu.domibus.ebms3.common.dao;

import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.validators.XmlValidationEventHandler;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;

/**
 * Created by Cosmin Baciu on 14-Sep-16.
 */
public class PModeProviderTest {

    @Test
    public void validatePMode() throws Exception {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(PModeProvider.SCHEMAS_DIR + PModeProvider.DOMIBUS_PMODE_XSD);
        Schema schema = sf.newSchema(new StreamSource(xsdStream));
        JAXBContext jaxbContext = JAXBContext.newInstance("eu.domibus.common.model.configuration");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new XmlValidationEventHandler());

        SAXParserFactory spf = SAXParserFactory.newInstance();

        spf.setNamespaceAware(true);
        spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

//        Source xmlSource = getPMode(spf);
        XMLEventReader xmlSource = getPModeReader();

        final Configuration configuration = (Configuration) unmarshaller.unmarshal(xmlSource);
        System.out.println(configuration);
    }

    protected Source getPMode(SAXParserFactory spf) throws ParserConfigurationException, SAXException, XMLStreamException {
        final InputStream pmodeXml = getClass().getClassLoader().getResourceAsStream("schemas/acc_domibus-configuration-EUCEG_000001.original.xml");
        Source xmlSource = new SAXSource(spf.newSAXParser().getXMLReader(), new InputSource(pmodeXml));
        return xmlSource;
    }

    protected XMLEventReader getPModeReader() throws ParserConfigurationException, SAXException, XMLStreamException {
        final InputStream pmodeXml = getClass().getClassLoader().getResourceAsStream("schemas/acc_domibus-configuration-EUCEG_000001.original2.xml");
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(pmodeXml);
        return eventReader;
//        return inputFactory.createFilteredReader(eventReader, new WhitespaceFilter());
    }
}
