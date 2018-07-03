package eu.domibus.common.validators;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Property;
import eu.domibus.common.model.configuration.PropertySet;
import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.xml.XMLUtilImpl;
import mockit.Expectations;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by idragusa on 7/2/18.
 */
@RunWith(JMockit.class)
public class PropertyProfileValidatorTest {

    public static final String valid4CornerMessagePath = "target/test-classes/eu/domibus/common/validators/valid4CornerMessage.xml";

    @Tested
    PropertyProfileValidator propertyProfileValidator;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    private LegConfiguration legConfiguration = new LegConfiguration();

    private PropertySet propertySet = new PropertySet();


    @Test
    public void validateTest() throws EbMS3Exception, FileNotFoundException, XMLStreamException, JAXBException, ParserConfigurationException, SAXException {
        Set<Property> properties = new HashSet<>();
        properties.add(createProperty(MessageConstants.ORIGINAL_SENDER, MessageConstants.ORIGINAL_SENDER, "String", true));
        properties.add(createProperty(MessageConstants.FINAL_RECIPIENT, MessageConstants.FINAL_RECIPIENT, "String", true));
        new NonStrictExpectations(legConfiguration, propertySet) {{
            domibusConfigurationService.isFourCornerEnabled();
            result = true;

            legConfiguration.getPropertySet();
            result = propertySet;

            propertySet.getProperties();
            result = properties;
        }};

        final Messaging messaging = createMessaging(new FileInputStream(new File(valid4CornerMessagePath)));
        propertyProfileValidator.validate(messaging, "anyKey");
    }


    @Test(expected = EbMS3Exception.class)
    public void validateMissingPropertyTest() throws EbMS3Exception, FileNotFoundException, XMLStreamException, JAXBException, ParserConfigurationException, SAXException {
        Set<Property> properties = new HashSet<>();
        properties.add(createProperty(MessageConstants.ORIGINAL_SENDER, MessageConstants.ORIGINAL_SENDER, "String", true));
        new NonStrictExpectations(legConfiguration, propertySet) {{
            domibusConfigurationService.isFourCornerEnabled();
            result = true;

            legConfiguration.getPropertySet();
            result = propertySet;

            propertySet.getProperties();
            result = properties;
        }};

        final Messaging messaging = createMessaging(new FileInputStream(new File(valid4CornerMessagePath)));
        propertyProfileValidator.validate(messaging, "anyKey");
    }

    @Test
    public void validate4CornerModelTest() throws EbMS3Exception, FileNotFoundException, XMLStreamException, JAXBException, ParserConfigurationException, SAXException {

        new Expectations() {{
            domibusConfigurationService.isFourCornerEnabled();
            result = true;

        }};

        final Messaging messaging = createMessaging(new FileInputStream(new File(valid4CornerMessagePath)));
        propertyProfileValidator.validateForCornerModel(messaging);
    }

    @Test(expected = EbMS3Exception.class)
    public void validate4CornerModelMissingMessagePropertiesTest() throws EbMS3Exception, FileNotFoundException, XMLStreamException, JAXBException, ParserConfigurationException, SAXException {

        new Expectations() {{
            domibusConfigurationService.isFourCornerEnabled();
            result = true;

        }};

        final Messaging messaging = createMessaging(new FileInputStream(new File(valid4CornerMessagePath)));
        messaging.getUserMessage().setMessageProperties(null);

        propertyProfileValidator.validateForCornerModel(messaging);
    }

    @Test(expected = EbMS3Exception.class)
    public void validate4CornerModelMissingOriginalSenderTest() throws EbMS3Exception, FileNotFoundException, XMLStreamException, JAXBException, ParserConfigurationException, SAXException {

        new Expectations() {{
            domibusConfigurationService.isFourCornerEnabled();
            result = true;

        }};

        final Messaging messaging = createMessaging(new FileInputStream(new File(valid4CornerMessagePath)));
        messaging.getUserMessage().getMessageProperties().getProperty().clear();

        propertyProfileValidator.validateForCornerModel(messaging);
    }

    private Property createProperty(String name, String key, String dataType, boolean required) {
        Property property = new Property();
        property.setName(name);
        property.setRequired(required);
        property.setKey(key);
        property.setDatatype(dataType);

        return property;
    }

    private Messaging createMessaging (InputStream inputStream) throws XMLStreamException, JAXBException, ParserConfigurationException, SAXException {
        XMLUtil xmlUtil = new XMLUtilImpl();
        JAXBContext jaxbContext = JAXBContext.newInstance(Messaging.class);
        JAXBElement root = xmlUtil.unmarshal(true, jaxbContext, inputStream, null).getResult();
        return (Messaging) root.getValue();
    }
}
