package eu.domibus.xml;

import eu.domibus.api.util.xml.UnmarshallerResult;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

import static org.junit.Assert.*;


/**
 * Created by Cosmin Baciu on 14-Sep-16.
 */
@RunWith(JMockit.class)
public class XMLUtilImplTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(XMLUtilImplTest.class);

    @Tested
    XMLUtilImpl xmlUtil;

    @Test
    public void testUnmarshalWithNoWhiteSpaces() throws Exception {
        UnmarshallerResult unmarshal = unmarshalPmode("samplePModes/domibus-configuration-valid.xml", false);
        assertTrue(unmarshal.getErrors() == null || unmarshal.getErrors().size() == 0);
        assertTrue(unmarshal.isValid());
    }



    @Test
    public void testUnmarshalSplittingWithNoWhiteSpaces() throws Exception {
        UnmarshallerResult unmarshal = unmarshalPmode("samplePModes/domibus-configuration-splitting-valid.xml", false);
        LOG.info(StringUtils.join(unmarshal.getErrors(), ";"));
        assertTrue(unmarshal.getErrors() == null || unmarshal.getErrors().size() == 0);
        assertTrue(unmarshal.isValid());
    }

    @Test
    public void testUnmarshalWithWhiteSpaces() throws Exception {
        UnmarshallerResult unmarshal = unmarshalPmode("samplePModes/domibus-configuration-with-whitespaces.xml", false);
        assertTrue(unmarshal.getErrors() != null && unmarshal.getErrors().size() > 0);
        LOG.debug("Validation errors: [" + unmarshal.getErrorMessage() + "]");
        assertFalse(unmarshal.isValid());
    }

    @Test
    public void testUnmarshalWithWhiteSpacesIgnored() throws Exception {
        UnmarshallerResult unmarshal = unmarshalPmode("samplePModes/domibus-configuration-with-whitespaces.xml", true);
        assertTrue(unmarshal.getErrors() == null || unmarshal.getErrors().size() == 0);
        assertTrue(unmarshal.isValid());
    }

    protected UnmarshallerResult unmarshalPmode(String resourceName, boolean ignoreWhiteSpaces) throws JAXBException, SAXException, ParserConfigurationException, XMLStreamException {
        InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(PModeProvider.SCHEMAS_DIR + PModeProvider.DOMIBUS_PMODE_XSD);
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        JAXBContext jaxbContext = JAXBContext.newInstance("eu.domibus.common.model.configuration");
        UnmarshallerResult unmarshal = xmlUtil.unmarshal(ignoreWhiteSpaces, jaxbContext, xmlStream, xsdStream);
        assertNotNull(unmarshal.getResult());
        assertTrue(unmarshal.getResult() instanceof Configuration);

        return unmarshal;
    }
}
