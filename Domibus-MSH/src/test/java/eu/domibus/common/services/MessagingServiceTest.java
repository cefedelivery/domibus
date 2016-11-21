package eu.domibus.common.services;

import eu.domibus.api.xml.XMLUtil;
import eu.domibus.common.dao.MessagingDao;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.SAXException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Ioana Dragusanu
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessagingServiceTest {

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
        final String validHeaderFilePath = "target/test-classes/eu/domibus/services/validMessaging.xml";
        final String validContentFilePath = "target/test-classes/eu/domibus/services/validContent.payload";
        final Messaging validMessaging = createMessaging(new FileInputStream(new File(validHeaderFilePath)));
        DataHandler dh = new DataHandler(new FileDataSource(new File(validContentFilePath)));
        ((PartInfo) (validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0])).setPayloadDatahandler(dh);

        messagingService.storeMessage(validMessaging);
        Assert.assertEquals(validMessaging.getUserMessage().getPayloadInfo().getPartInfo().size(), 1);
        PartInfo partInfo = (PartInfo) validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0];

        byte[] expectedBinaryData = Files.readAllBytes(Paths.get(validContentFilePath));
        Assert.assertEquals(new String(partInfo.getBinaryData()), new String(expectedBinaryData));

        new Verifications() {{
            messagingDao.create(validMessaging);
            times = 1;
        }};
    }

    @Test
    public void testStoreValidMessageToStorageDirectory() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException {
        messagingService.setStorage(new Storage(new File("target/test-classes/eu/domibus/services/")));
        final String validHeaderFilePath = "target/test-classes/eu/domibus/services/validMessaging.xml";
        final String validContentFilePath = "target/test-classes/eu/domibus/services/validContent.payload";
        final Messaging validMessaging = createMessaging(new FileInputStream(new File(validHeaderFilePath)));
        DataHandler dh = new DataHandler(new FileDataSource(new File(validContentFilePath)));
        ((PartInfo) (validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0])).setPayloadDatahandler(dh);

        messagingService.storeMessage(validMessaging);
        Assert.assertEquals(validMessaging.getUserMessage().getPayloadInfo().getPartInfo().size(), 1);
        PartInfo partInfo = (PartInfo) validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0];
        byte[] result = Files.readAllBytes(Paths.get(partInfo.getFileName()));

        byte[] expectedBinaryData = Files.readAllBytes(Paths.get(validContentFilePath));
        Assert.assertEquals(new String(result), new String(expectedBinaryData));

        new Verifications() {{
            messagingDao.create(validMessaging);
            times = 1;
        }};
    }

    @Test
    public void testStoreValidMessageCompressedWithStorageDirectory() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException {
        messagingService.setStorage(new Storage(new File("target/test-classes/eu/domibus/services/")));

        final String validHeaderFilePath = "target/test-classes/eu/domibus/services/validMessaging.xml";
        final String validContentFilePath = "target/test-classes/eu/domibus/services/validContent.payload";
        final Messaging validMessaging = createMessaging(new FileInputStream(new File(validHeaderFilePath)));
        DataHandler dh = new DataHandler(new FileDataSource(new File(validContentFilePath)));
        ((PartInfo) (validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0])).setPayloadDatahandler(dh);
        Property property = new Property();
        property.setName(CompressionService.COMPRESSION_PROPERTY_KEY);
        property.setValue(CompressionService.COMPRESSION_PROPERTY_VALUE);
        ((PartInfo) validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0]).getPartProperties().getProperties().add(property);

        messagingService.storeMessage(validMessaging);
        Assert.assertEquals(validMessaging.getUserMessage().getPayloadInfo().getPartInfo().size(), 1);
        PartInfo partInfo = (PartInfo) validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0];
        byte[] result = Files.readAllBytes(Paths.get(partInfo.getFileName()));

        byte[] expectedCompressedData = MessagingUtils.compress(validContentFilePath);
        Assert.assertEquals(new String(expectedCompressedData), new String(result));

        new Verifications() {{
            messagingDao.create(validMessaging);
            times = 1;
        }};
    }

    @Test
    public void testStoreValidMessageCompressed() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException {
        final String validHeaderFilePath = "target/test-classes/eu/domibus/services/validMessaging.xml";
        final String validContentFilePath = "target/test-classes/eu/domibus/services/validContent.payload";
        final Messaging validMessaging = createMessaging(new FileInputStream(new File(validHeaderFilePath)));
        DataHandler dh = new DataHandler(new FileDataSource(new File(validContentFilePath)));
        ((PartInfo) (validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0])).setPayloadDatahandler(dh);
        Property property = new Property();
        property.setName(CompressionService.COMPRESSION_PROPERTY_KEY);
        property.setValue(CompressionService.COMPRESSION_PROPERTY_VALUE);
        ((PartInfo) validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0]).getPartProperties().getProperties().add(property);

        messagingService.storeMessage(validMessaging);
        Assert.assertEquals(validMessaging.getUserMessage().getPayloadInfo().getPartInfo().size(), 1);
        PartInfo partInfo = (PartInfo) validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0];

        byte[] expectedCompressedData = MessagingUtils.compress(validContentFilePath);
        Assert.assertEquals(new String(expectedCompressedData), new String(partInfo.getBinaryData()));

        new Verifications() {{
            messagingDao.create(validMessaging);
            times = 1;
        }};
    }


    private Messaging createMessaging (InputStream inputStream) throws XMLStreamException, JAXBException, ParserConfigurationException, SAXException {
        XMLUtil xmlUtil = new XMLUtilImpl();
        JAXBContext jaxbContext = JAXBContext.newInstance(Messaging.class);
        JAXBElement root = xmlUtil.unmarshal(true, jaxbContext, inputStream, null).getResult();
        return (Messaging) root.getValue();
    }
}
