package eu.domibus.plugin.fs;

import eu.domibus.plugin.fs.ebms3.ObjectFactory;
import eu.domibus.plugin.fs.ebms3.UserMessage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public abstract class FSTestHelper {

    public static UserMessage getUserMessage(Class<?> testClass, String resourceName) throws JAXBException {
        InputStream metadata = getTestResource(testClass, resourceName);
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller um = jaxbContext.createUnmarshaller();
        StreamSource streamSource = new StreamSource(metadata);
        JAXBElement<UserMessage> jaxbElement = um.unmarshal(streamSource, UserMessage.class);
        return jaxbElement.getValue();
    }

    public static UserMessage getUserMessage(InputStream inputStream) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
        Unmarshaller um = jaxbContext.createUnmarshaller();
        StreamSource streamSource = new StreamSource(inputStream);
        JAXBElement<UserMessage> jaxbElement = um.unmarshal(streamSource, UserMessage.class);
        return jaxbElement.getValue();
    }


    public static InputStream getTestResource(Class<?> testClass, String resourceName) {
        return testClass.getResourceAsStream(String.format("%s_%s", testClass.getSimpleName(), resourceName));
    }

}
