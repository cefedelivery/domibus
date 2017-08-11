package eu.domibus.plugin.fs;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.vfs2.FileSystemException;
import org.xml.sax.SAXException;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.ebms3.ObjectFactory;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSXMLHelper {
    
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSXMLHelper.class);
    
    private static final String XSD_FILES_LOCATION = "xsd";
    private static final String DEFAULT_SCHEMA = "ebms3.xsd";
    
    private FSXMLHelper() {
        super();
    }
    
    public static <T> T parseXML(InputStream inputStream, Class<T> clazz) throws JAXBException, FileSystemException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller um = jaxbContext.createUnmarshaller();
        
        try {
            Schema schema = loadSchema(DEFAULT_SCHEMA);
            um.setSchema(schema);
        } catch (SAXException ex) {
            LOG.warn("Error loading default schema", ex);
        }
        
        StreamSource streamSource = new StreamSource(inputStream);
        JAXBElement<T> jaxbElement = um.unmarshal(streamSource, clazz);

        return jaxbElement.getValue();
    }

    public static Schema loadSchema(String schemaName) throws SAXException {
        return loadSchema(FSXMLHelper.class.getClassLoader().getResourceAsStream(XSD_FILES_LOCATION + "/" + schemaName));
    }
    
    public static Schema loadSchema(InputStream inputStream) throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        StreamSource schemaSource = new StreamSource(inputStream);
        Schema schema = schemaFactory.newSchema(schemaSource);
        return schema;
    }
}
