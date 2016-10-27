package eu.domibus.common.services;

import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.services.impl.MessagingServiceImpl;
import eu.domibus.configuration.Storage;
import eu.domibus.ebms3.common.model.CompressionService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.Property;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

/**
 * Created by idragusa on 10/26/16.
 */
@RunWith(JMockit.class)
public class MessagingServiceTest {

    @Tested
    MessagingService messagingService = new MessagingServiceImpl();

    @Injectable
    MessagingDao messagingDao;

    @Test
    public void testStoreMessageCalls(@Injectable final Messaging messaging) throws IOException, JAXBException, XMLStreamException {
        messagingService.storeMessage(messaging);

        new Verifications() {{
            messagingDao.create(messaging);
            times = 1;
        }};
    }

    @Test
    public void testStoreValidMessage() throws IOException, JAXBException, XMLStreamException {
        final String validHeaderFilePath = "target/test-classes/eu/domibus/services/validMessaging.xml";
        final String validContentFilePath = "target/test-classes/eu/domibus/services/validContent.payload";
        final Messaging validMessaging = (Messaging) unmarshall(new FileInputStream(new File(validHeaderFilePath)), Messaging.class);
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
    public void testStoreValidMessageToStorageDirectory() throws IOException, JAXBException, XMLStreamException {
        Storage.storageDirectory = new File("target/test-classes/eu/domibus/services/");
        final String validHeaderFilePath = "target/test-classes/eu/domibus/services/validMessaging.xml";
        final String validContentFilePath = "target/test-classes/eu/domibus/services/validContent.payload";
        final Messaging validMessaging = (Messaging) unmarshall(new FileInputStream(new File(validHeaderFilePath)), Messaging.class);
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
    public void testStoreValidMessageCompressed() throws IOException, JAXBException, XMLStreamException {
        Storage.storageDirectory = new File("target/test-classes/eu/domibus/services/");

        final String validHeaderFilePath = "target/test-classes/eu/domibus/services/validMessaging.xml";
        final String validContentFilePath = "target/test-classes/eu/domibus/services/validContent.payload";
        final Messaging validMessaging = (Messaging) unmarshall(new FileInputStream(new File(validHeaderFilePath)), Messaging.class);
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

        byte[] expectedCompressedData = compress(Files.readAllBytes(Paths.get(validContentFilePath)));
        Assert.assertEquals(new String(expectedCompressedData), new String(result));

        new Verifications() {{
            messagingDao.create(validMessaging);
            times = 1;
        }};
    }

    @Test
    public void testStoreValidMessageCompressedWithStorageDirectory() throws IOException, JAXBException, XMLStreamException {
        final String validHeaderFilePath = "target/test-classes/eu/domibus/services/validMessaging.xml";
        final String validContentFilePath = "target/test-classes/eu/domibus/services/validContent.payload";
        final Messaging validMessaging = (Messaging) unmarshall(new FileInputStream(new File(validHeaderFilePath)), Messaging.class);
        DataHandler dh = new DataHandler(new FileDataSource(new File(validContentFilePath)));
        ((PartInfo) (validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0])).setPayloadDatahandler(dh);
        Property property = new Property();
        property.setName(CompressionService.COMPRESSION_PROPERTY_KEY);
        property.setValue(CompressionService.COMPRESSION_PROPERTY_VALUE);
        ((PartInfo) validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0]).getPartProperties().getProperties().add(property);

        messagingService.storeMessage(validMessaging);
        Assert.assertEquals(validMessaging.getUserMessage().getPayloadInfo().getPartInfo().size(), 1);
        PartInfo partInfo = (PartInfo) validMessaging.getUserMessage().getPayloadInfo().getPartInfo().toArray()[0];

        byte[] expectedCompressedData = compress(Files.readAllBytes(Paths.get(validContentFilePath)));
        Assert.assertEquals(new String(expectedCompressedData), new String(partInfo.getBinaryData()));

        new Verifications() {{
            messagingDao.create(validMessaging);
            times = 1;
        }};
    }

    private byte[] compress(byte[] data) throws IOException{
            final byte[] buffer = new byte[1024];
            InputStream sourceStream = new ByteArrayInputStream(data);
            ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
            GZIPOutputStream targetStream = new GZIPOutputStream(compressedContent);
            int i;
            while ((i = sourceStream.read(buffer)) > 0) {
                targetStream.write(buffer, 0, i);
            }
            sourceStream.close();
            targetStream.finish();
            targetStream.close();
            byte[] result = compressedContent.toByteArray();
            return result;
    }

    private static Object unmarshall (InputStream inputStream, Class type) throws XMLStreamException, JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Source source = new StreamSource(inputStream);
        JAXBElement root = unmarshaller.unmarshal(source, type);
        Object object = root.getValue();
        return object;
    }
}
