package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.configuration.storage.StorageProvider;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.messaging.MessagingUtils;
import eu.domibus.xml.XMLUtilImpl;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Ioana Dragusanu
 * @author Cosmin Baciu
 * @since 3.3
 */

@RunWith(JMockit.class)
public class MessagingServiceImplTest {

    public static final String validHeaderFilePath = "target/test-classes/eu/domibus/services/validMessaging.xml";
    public static final String validContentFilePath = "target/test-classes/eu/domibus/services/validContent.payload";
    public static final String STORAGE_PATH = "target/test-classes/eu/domibus/services/";

    @Tested
    MessagingServiceImpl messagingService;

    @Injectable
    MessagingDao messagingDao;

    @Injectable
    Storage storage;

    @Injectable
    StorageProvider storageProvider;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    LegConfiguration legConfiguration;

    @Injectable
    SplitAndJoinService splitAndJoinService;

    @Injectable
    CompressionService compressionService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    DomainTaskExecutor domainTaskExecutor;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    BackendNotificationService backendNotificationService;

    @Injectable
    UserMessageLogDao userMessageLogDao;

    @Test
    public void testStoreOutgoingPayloadToDatabase(@Injectable UserMessage userMessage,
                                                   @Injectable PartInfo partInfo,
                                                   @Injectable LegConfiguration legConfiguration,
                                                   @Injectable String backendName) throws IOException, EbMS3Exception {
        new Expectations(messagingService) {{
            storageProvider.idPayloadsPersistenceInDatabaseConfigured();
            result = true;

            messagingService.saveOutgoingPayloadToDatabase(partInfo, userMessage, legConfiguration, backendName);
        }};


        messagingService.storeOutgoingPayload(partInfo, userMessage, legConfiguration, backendName);

        new Verifications() {{
            messagingService.saveOutgoingPayloadToDatabase(partInfo, userMessage, legConfiguration, backendName);
            times = 1;
        }};
    }

    @Test
    public void testSaveIncomingPayloadToDisk(@Injectable PartInfo partInfo,
                                              @Injectable Storage storage,
                                              @Mocked File file,
                                              @Injectable InputStream inputStream,
                                              @Mocked UUID uuid) throws IOException {

        String path = "/home/invoice.pdf";
        new Expectations(messagingService) {{
            new File((File) any, anyString);
            result = file;

            file.getAbsolutePath();
            result = path;

            partInfo.getPayloadDatahandler().getInputStream();
            result = inputStream;

            messagingService.saveIncomingFileToDisk(file, inputStream);
        }};

        messagingService.saveIncomingPayloadToDisk(partInfo, storage);

        new Verifications() {{
            messagingService.saveIncomingFileToDisk(file, inputStream);
            times = 1;

            partInfo.setFileName(path);
        }};
    }

    @Test
    public void testSaveIncomingPayloadToDatabase(@Injectable PartInfo partInfo,
                                                  @Injectable Storage storage,
                                                  @Mocked IOUtils ioUtils,
                                                  @Injectable InputStream inputStream) throws IOException {
        final byte[] binaryData = "test".getBytes();

        new Expectations(messagingService) {{
            partInfo.getPayloadDatahandler().getInputStream();
            result = inputStream;

            IOUtils.toByteArray(inputStream);
            result = binaryData;
        }};

        messagingService.saveIncomingPayloadToDatabase(partInfo);

        new Verifications() {{
            partInfo.setBinaryData(binaryData);
            partInfo.setLength(binaryData.length);
            partInfo.setFileName(null);
        }};
    }

    @Test
    public void testStoreIncomingPayloadToDatabase(@Injectable UserMessage userMessage,
                                                   @Injectable PartInfo partInfo) throws IOException {
        new Expectations(messagingService) {{
            storageProvider.idPayloadsPersistenceInDatabaseConfigured();
            result = true;

            messagingService.saveIncomingPayloadToDatabase(partInfo);
        }};


        messagingService.storeIncomingPayload(partInfo, userMessage);

        new Verifications() {{
            messagingService.saveIncomingPayloadToDatabase(partInfo);
            times = 1;
        }};
    }

    @Test
    public void testStoreIncomingPayloadToFileSystem(@Injectable UserMessage userMessage,
                                                     @Injectable PartInfo partInfo,
                                                     @Injectable Storage storage) throws IOException {
        new Expectations(messagingService) {{
            storageProvider.idPayloadsPersistenceInDatabaseConfigured();
            result = false;

            storageProvider.getCurrentStorage();
            result = storage;

            messagingService.saveIncomingPayloadToDisk(partInfo, storage);
        }};


        messagingService.storeIncomingPayload(partInfo, userMessage);

        new Verifications() {{
            messagingService.saveIncomingPayloadToDisk(partInfo, storage);
            times = 1;
        }};
    }


    @Test
    public void testStoreSourceMessagePayloads(@Injectable Messaging messaging,
                                               @Injectable MSHRole mshRole,
                                               @Injectable LegConfiguration legConfiguration,
                                               @Injectable String backendName) {

        String messageId = "123";
        new Expectations(messagingService) {{
            messaging.getUserMessage().getMessageInfo().getMessageId();
            result = messageId;

            messagingService.storePayloads(messaging, mshRole, legConfiguration, backendName);
        }};

        messagingService.storeSourceMessagePayloads(messaging, mshRole, legConfiguration, backendName);

        new Verifications() {{
            userMessageService.scheduleSourceMessageSending(messageId);
        }};
    }

    @Test
    public void testScheduleSourceMessagePayloads(@Injectable final Messaging messaging,
                                                  @Injectable final Domain domain,
                                                  @Injectable final PayloadInfo payloadInfo,
                                                  @Injectable final PartInfo partInfo) {


        List<PartInfo> partInfos = new ArrayList<>();
        partInfos.add(partInfo);

        new Expectations() {{
            messaging.getUserMessage().getPayloadInfo();
            result = payloadInfo;

            payloadInfo.getPartInfo();
            result = partInfos;

            partInfo.getLength();
            result = 20 * MessagingServiceImpl.BYTES_IN_MB;

            domibusPropertyProvider.getLongDomainProperty(domain, MessagingServiceImpl.PROPERTY_PAYLOADS_SCHEDULE_THRESHOLD);
            result = 15;
        }};

        final boolean scheduleSourceMessagePayloads = messagingService.scheduleSourceMessagePayloads(messaging, domain);
        Assert.assertTrue(scheduleSourceMessagePayloads);
    }

    @Test
    public void testStoreMessageCalls(@Injectable final Messaging messaging) throws IOException, JAXBException, XMLStreamException {
        messagingService.storeMessage(messaging, MSHRole.SENDING, legConfiguration, "backend");

        new Verifications() {{
            messagingDao.create(messaging);
            times = 1;
        }};
    }

    @Test
    public void testStoreValidMessage() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException {
        new Expectations() {{
            storageProvider.idPayloadsPersistenceInDatabaseConfigured();
            result = true;
        }};

        PartInfo partInfo = storeValidMessage();
        byte[] expectedBinaryData = Files.readAllBytes(Paths.get(validContentFilePath));
        Assert.assertEquals(new String(expectedBinaryData), new String(partInfo.getBinaryData()));
    }

    @Test
    public void testStoreValidMessageToStorageDirectory() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException {
        new Expectations() {{
            storageProvider.getCurrentStorage();
            result = new Storage(new File(STORAGE_PATH));


        }};

        PartInfo partInfo = storeValidMessage();
        byte[] expectedBinaryData = Files.readAllBytes(Paths.get(validContentFilePath));
        byte[] result = Files.readAllBytes(Paths.get(partInfo.getFileName()));
        Assert.assertEquals(new String(expectedBinaryData), new String(result));
    }

    @Test
    public void testStoreValidMessageCompressedWithStorageDirectory() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException, EbMS3Exception {
        new Expectations() {{
            storageProvider.getCurrentStorage();
            result = new Storage(new File(STORAGE_PATH));

            compressionService.handleCompression(anyString, withAny(new PartInfo()), legConfiguration);
            result = true;
        }};
        PartInfo partInfo = storeValidMessage(true);
        byte[] expectedCompressedData = MessagingUtils.compress(validContentFilePath);
        byte[] result = Files.readAllBytes(Paths.get(partInfo.getFileName()));
        Assert.assertEquals(new String(expectedCompressedData), new String(result));
    }

    @Test
    public void testStoreValidMessageCompressed() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException, EbMS3Exception {
        new Expectations() {{
            compressionService.handleCompression(anyString, withAny(new PartInfo()), legConfiguration);
            result = true;

            storageProvider.idPayloadsPersistenceInDatabaseConfigured();
            result = true;
        }};

        PartInfo partInfo = storeValidMessage(true);
        byte[] expectedCompressedData = MessagingUtils.compress(validContentFilePath);
        Assert.assertEquals(new String(expectedCompressedData), new String(partInfo.getBinaryData()));
    }

    @Test(expected = CompressionException.class)
    public void testStoreInvalidMessage() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException {
        PartInfo partInfo = storeInvalidMessage();
        Assert.assertFalse("A CompressionException should have been raised before", true);
    }

    private Messaging createMessaging(InputStream inputStream) throws XMLStreamException, JAXBException, ParserConfigurationException, SAXException {
        XMLUtil xmlUtil = new XMLUtilImpl();
        JAXBContext jaxbContext = JAXBContext.newInstance(Messaging.class);
        JAXBElement root = xmlUtil.unmarshal(true, jaxbContext, inputStream, null).getResult();
        return (Messaging) root.getValue();
    }

    private PartInfo getOnePartInfo(Messaging messaging) {
        /* Check there is only one partInfo */
        Assert.assertEquals(1, messaging.getUserMessage().getPayloadInfo().getPartInfo().size());
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
        if (isCompressed) {
            Property property = new Property();
            property.setName(CompressionService.COMPRESSION_PROPERTY_KEY);
            property.setValue(CompressionService.COMPRESSION_PROPERTY_VALUE);
            partInfo.getPartProperties().getProperties().add(property);
        }

        messagingService.storeMessage(validMessaging, MSHRole.SENDING, legConfiguration, "backend");
        partInfo = getOnePartInfo(validMessaging);

        return partInfo;
    }

    private PartInfo storeInvalidMessage() throws IOException, JAXBException, XMLStreamException, ParserConfigurationException, SAXException, FileNotFoundException {
        final Messaging messaging = createMessaging(new FileInputStream(new File(validHeaderFilePath)));
        DataHandler dh = new DataHandler(new URL("http://invalid.url"));

        PartInfo partInfo = getOnePartInfo(messaging);
        partInfo.setPayloadDatahandler(dh);

        messagingService.storeMessage(messaging, MSHRole.SENDING, legConfiguration, "backend");
        partInfo = getOnePartInfo(messaging);

        return partInfo;
    }
}
