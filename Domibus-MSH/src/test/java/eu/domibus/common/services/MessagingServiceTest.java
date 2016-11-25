package eu.domibus.common.services;

import eu.domibus.api.xml.XMLUtil;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.services.impl.CompressionService;
import eu.domibus.common.services.impl.MessagingServiceImpl;
import eu.domibus.configuration.Storage;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.messaging.MessagingUtils;
import eu.domibus.xml.XMLUtilImpl;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Ioana Dragusanu
 * @since 3.3
 */

@RunWith(JMockit.class)
public class MessagingServiceTest {

    public static final String validHeaderFilePath = "target/test-classes/eu/domibus/services/validMessaging.xml";
    public static final String validContentFilePath = "target/test-classes/eu/domibus/services/validContent.payload";
    public static final String STORAGE_PATH = "target/test-classes/eu/domibus/services/";

    @Tested
    MessagingService messagingService = new MessagingServiceImpl();

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    Storage storage;

    @Test
    public void testStoreMessageCalls(@Injectable final Messaging messaging) throws IOException, JAXBException, XMLStreamException {
        messagingService.storeMessage(messaging);

        new Verifications() {{
            messagingDao.create(messaging);
            times = 1;
        }};
    }

    @Test
    public void testStoreValidMessage() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException {
        PartInfo partInfo = storeValidMessage();
        byte[] expectedBinaryData = Files.readAllBytes(Paths.get(validContentFilePath));
        Assert.assertEquals(new String(expectedBinaryData), new String(partInfo.getBinaryData()));
    }

    @Test
    public void testStoreValidMessageToStorageDirectory() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException {
        messagingService.setStorage(new Storage(new File(STORAGE_PATH)));
        PartInfo partInfo = storeValidMessage();
        byte[] expectedBinaryData = Files.readAllBytes(Paths.get(validContentFilePath));
        byte[] result = Files.readAllBytes(Paths.get(partInfo.getFileName()));
        Assert.assertEquals(new String(expectedBinaryData), new String(result));
    }

    @Test
    public void testStoreValidMessageCompressedWithStorageDirectory() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException {
        messagingService.setStorage(new Storage(new File(STORAGE_PATH)));
        PartInfo partInfo = storeValidMessage(true);
        byte[] expectedCompressedData = MessagingUtils.compress(validContentFilePath);
        byte[] result = Files.readAllBytes(Paths.get(partInfo.getFileName()));
        Assert.assertEquals(new String(expectedCompressedData), new String(result));
    }

    @Test
    public void testStoreValidMessageCompressed() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException {
        PartInfo partInfo = storeValidMessage(true);
        byte[] expectedCompressedData = MessagingUtils.compress(validContentFilePath);
        Assert.assertEquals(new String(expectedCompressedData), new String(partInfo.getBinaryData()));
    }

    @Test(expected = CompressionException.class)
    public void testStoreInvalidMessage() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException {
        PartInfo partInfo = storeInvalidMessage();
        Assert.assertFalse("A CompressionException should have been raised before", true);
    }

    private Messaging createMessaging (InputStream inputStream) throws XMLStreamException, JAXBException, ParserConfigurationException, SAXException {
        XMLUtil xmlUtil = new XMLUtilImpl();
        JAXBContext jaxbContext = JAXBContext.newInstance(Messaging.class);
        JAXBElement root = xmlUtil.unmarshal(true, jaxbContext, inputStream, null).getResult();
        return (Messaging) root.getValue();
    }

    private PartInfo getOnePartInfo(Messaging messaging) {
        /* Check there is only one partInfo */
        Assert.assertEquals(messaging.getUserMessage().getPayloadInfo().getPartInfo().size(), 1);
        /* return the only partInfo in the message */
        return (PartInfo) messaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0];
    }

    private PartInfo storeValidMessage() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException, FileNotFoundException {
        return storeValidMessage(false);
    }

    private PartInfo storeValidMessage(boolean isCompressed) throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException, FileNotFoundException {
        final Messaging validMessaging = createMessaging(new FileInputStream(new File(validHeaderFilePath)));
        DataHandler dh = new DataHandler(new FileDataSource(new File(validContentFilePath)));

        PartInfo partInfo = getOnePartInfo(validMessaging);
        partInfo.setPayloadDatahandler(dh);
        if(isCompressed) {
            Property property = new Property();
            property.setName(CompressionService.COMPRESSION_PROPERTY_KEY);
            property.setValue(CompressionService.COMPRESSION_PROPERTY_VALUE);
            partInfo.getPartProperties().getProperties().add(property);
        }

        messagingService.storeMessage(validMessaging);
        partInfo = getOnePartInfo(validMessaging);

        return partInfo;
    }

    private PartInfo storeInvalidMessage() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException, FileNotFoundException {
        final Messaging messaging = createMessaging(new FileInputStream(new File(validHeaderFilePath)));
        DataHandler dh = new DataHandler(new URL("http://invalid.url"));

        PartInfo partInfo = getOnePartInfo(messaging);
        partInfo.setPayloadDatahandler(dh);

        messagingService.storeMessage(messaging);
        partInfo = getOnePartInfo(messaging);

        return partInfo;
    }
}
