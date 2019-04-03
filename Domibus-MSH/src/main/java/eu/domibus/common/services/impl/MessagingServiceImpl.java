package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessagingService;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.configuration.storage.StorageProvider;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.receiver.BackendNotificationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * @author Ioana Dragusanu
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class MessagingServiceImpl implements MessagingService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingServiceImpl.class);
    public static final String PAYLOAD_EXTENSION = ".payload";
    public static final String MIME_TYPE_APPLICATION_UNKNOWN = "application/unknown";
    public static final String PROPERTY_PAYLOADS_SCHEDULE_THRESHOLD = "domibus.dispatcher.splitAndJoin.payloads.schedule.threshold";
    public static final int DEFAULT_BUFFER_SIZE = 32 * 1024;
    protected static Long BYTES_IN_MB = 1048576L;


    @Autowired
    protected MessagingDao messagingDao;

    @Autowired
    protected StorageProvider storageProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    private CompressionService compressionService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected UserMessageService userMessageService;

    @Autowired
    protected BackendNotificationService backendNotificationService;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public void storeMessage(Messaging messaging, MSHRole mshRole, final LegConfiguration legConfiguration, String backendName) throws CompressionException {
        if (messaging == null || messaging.getUserMessage() == null) {
            return;
        }

        if (MSHRole.SENDING == mshRole && messaging.getUserMessage().isSourceMessage()) {
            final Domain currentDomain = domainContextProvider.getCurrentDomain();

            if (scheduleSourceMessagePayloads(messaging, currentDomain)) {
                //stores the payloads asynchronously
                domainTaskExecutor.submitLongRunningTask(
                        () -> {
                            LOG.debug("Scheduling the SourceMessage saving");
                            storeSourceMessagePayloads(messaging, mshRole, legConfiguration, backendName);
                        },
                        () -> splitAndJoinService.setSourceMessageAsFailed(messaging.getUserMessage()),
                        currentDomain);
            } else {
                //stores the payloads synchronously
                storeSourceMessagePayloads(messaging, mshRole, legConfiguration, backendName);
            }
        } else {
            storePayloads(messaging, mshRole, legConfiguration, backendName);
        }
        LOG.debug("Saving Messaging");
        setPayloadsContentType(messaging);
        messagingDao.create(messaging);
    }

    protected boolean scheduleSourceMessagePayloads(Messaging messaging, final Domain domain) {
        final PayloadInfo payloadInfo = messaging.getUserMessage().getPayloadInfo();
        final List<PartInfo> partInfos = payloadInfo.getPartInfo();
        if (payloadInfo == null || partInfos == null || partInfos.isEmpty()) {
            LOG.debug("SourceMessages does not have any payloads");
            return false;
        }

        long totalPayloadLength = 0;
        for (PartInfo partInfo : partInfos) {
            totalPayloadLength += partInfo.getLength();
        }
        LOG.debug("SourceMessage payloads totalPayloadLength(bytes) [{}]", totalPayloadLength);

        final Long payloadsScheduleThresholdMB = domibusPropertyProvider.getLongDomainProperty(domain, PROPERTY_PAYLOADS_SCHEDULE_THRESHOLD);
        LOG.debug("Using configured payloadsScheduleThresholdMB [{}]", payloadsScheduleThresholdMB);

        final Long payloadsScheduleThresholdBytes = payloadsScheduleThresholdMB * BYTES_IN_MB;
        if (totalPayloadLength > payloadsScheduleThresholdBytes) {
            LOG.debug("The SourceMessage payloads will be scheduled for saving");
            return true;
        }
        return false;

    }

    protected void storeSourceMessagePayloads(Messaging messaging, MSHRole mshRole, LegConfiguration legConfiguration, String backendName) {
        LOG.debug("Saving the SourceMessage payloads");

        storePayloads(messaging, mshRole, legConfiguration, backendName);

        final String messageId = messaging.getUserMessage().getMessageInfo().getMessageId();
        LOG.debug("Scheduling the SourceMessage sending");
        userMessageService.scheduleSourceMessageSending(messageId);
    }

    protected void setPayloadsContentType(Messaging messaging) {
        if (messaging.getUserMessage().getPayloadInfo() == null || messaging.getUserMessage().getPayloadInfo().getPartInfo() == null) {
            return;
        }
        for (PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
            setContentType(partInfo);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public void storePayloads(Messaging messaging, MSHRole mshRole, LegConfiguration legConfiguration, String backendName) {
        LOG.debug("Storing payloads");
        if (messaging.getUserMessage().getPayloadInfo() != null && messaging.getUserMessage().getPayloadInfo().getPartInfo() != null) {
            for (PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
                try {
                    if (MSHRole.RECEIVING.equals(mshRole)) {
                        storeIncomingPayload(partInfo, messaging.getUserMessage());
                    } else {
                        storeOutgoingPayload(partInfo, messaging.getUserMessage(), legConfiguration, backendName);
                    }
                } catch (IOException | EbMS3Exception exc) {
                    LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, partInfo.getHref());
                    throw new CompressionException("Could not store binary data for message " + exc.getMessage(), exc);
                }
            }
        }
        LOG.debug("Finished storing payloads");
    }

    protected void storeIncomingPayload(PartInfo partInfo, UserMessage userMessage) throws IOException {
        String messageId = userMessage.getMessageInfo().getMessageId();

        if (storageProvider.idPayloadsPersistenceInDatabaseConfigured()) {
            saveIncomingPayloadToDatabase(partInfo);
        } else {
            if (StringUtils.isBlank(partInfo.getFileName())) {
                Storage currentStorage = storageProvider.getCurrentStorage();
                saveIncomingPayloadToDisk(partInfo, currentStorage);
            } else {
                LOG.debug("Incoming payload [{}] is already saved on file disk under [{}]", partInfo.getHref(), partInfo.getFileName());
            }
        }

        // Log Payload size
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIVED_PAYLOAD_SIZE, partInfo.getHref(), messageId, partInfo.getLength());
    }

    protected void saveIncomingPayloadToDisk(PartInfo partInfo, Storage currentStorage) throws IOException {
        LOG.debug("Saving incoming payload [{}] to file disk", partInfo.getHref());

        final File attachmentStore = new File(currentStorage.getStorageDirectory(), UUID.randomUUID().toString() + ".payload");
        partInfo.setFileName(attachmentStore.getAbsolutePath());
        try (final InputStream inputStream = partInfo.getPayloadDatahandler().getInputStream()) {
            final long fileLength = saveIncomingFileToDisk(attachmentStore, inputStream);
            partInfo.setLength(fileLength);
        }

        LOG.debug("Finished saving incoming payload [{}] to file disk", partInfo.getHref());
    }

    protected void saveIncomingPayloadToDatabase(PartInfo partInfo) throws IOException {
        LOG.debug("Saving incoming payload [{}] to database", partInfo.getHref());
        try (InputStream is = partInfo.getPayloadDatahandler().getInputStream()) {
            byte[] binaryData = IOUtils.toByteArray(is);
            partInfo.setBinaryData(binaryData);
            partInfo.setLength(binaryData.length);
            partInfo.setFileName(null);
        }
        LOG.debug("Finished saving incoming payload [{}] to database", partInfo.getHref());
    }


    protected void storeOutgoingPayload(PartInfo partInfo, UserMessage userMessage, final LegConfiguration legConfiguration, String backendName) throws IOException, EbMS3Exception {
        String messageId = userMessage.getMessageInfo().getMessageId();

        if (storageProvider.idPayloadsPersistenceInDatabaseConfigured()) {
            saveOutgoingPayloadToDatabase(partInfo, userMessage, legConfiguration, backendName);
        } else {
            //message fragment files are already saved on the file system
            if (!userMessage.isUserMessageFragment()) {
                Storage currentStorage = storageProvider.getCurrentStorage();
                saveOutgoingPayloadToDisk(partInfo, userMessage, legConfiguration, currentStorage, backendName);
            }
        }

        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SENDING_PAYLOAD_SIZE, partInfo.getHref(), messageId, partInfo.getLength());

        final boolean hasCompressionProperty = hasCompressionProperty(partInfo);
        if (hasCompressionProperty) {
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION, partInfo.getHref());
        }
    }


    protected void saveOutgoingPayloadToDisk(PartInfo partInfo, UserMessage userMessage, LegConfiguration legConfiguration, Storage currentStorage, String backendName) throws IOException, EbMS3Exception {
        LOG.debug("Saving outgoing payload [{}] to file disk", partInfo.getHref());

        try (InputStream is = partInfo.getPayloadDatahandler().getInputStream()) {
            final String originalFileName = partInfo.getFileName();

            backendNotificationService.notifyPayloadSubmitted(userMessage, originalFileName, partInfo, backendName);

            final File attachmentStore = new File(currentStorage.getStorageDirectory(), UUID.randomUUID().toString() + PAYLOAD_EXTENSION);
            partInfo.setFileName(attachmentStore.getAbsolutePath());
            final long fileLength = saveOutgoingFileToDisk(attachmentStore, partInfo, is, userMessage, legConfiguration);
            partInfo.setLength(fileLength);

            LOG.debug("Finished saving outgoing payload [{}] to file disk", partInfo.getHref());

            backendNotificationService.notifyPayloadProcessed(userMessage, originalFileName, partInfo, backendName);
        }
    }

    protected void saveOutgoingPayloadToDatabase(PartInfo partInfo, UserMessage userMessage, LegConfiguration legConfiguration, String backendName) throws IOException, EbMS3Exception {
        LOG.debug("Saving outgoing payload [{}] to database", partInfo.getHref());

        try (InputStream is = partInfo.getPayloadDatahandler().getInputStream()) {
            final String originalFileName = partInfo.getFileName();

            backendNotificationService.notifyPayloadSubmitted(userMessage, originalFileName, partInfo, backendName);

            byte[] binaryData = getOutgoingBinaryData(partInfo, is, userMessage, legConfiguration);
            partInfo.setBinaryData(binaryData);
            partInfo.setLength(binaryData.length);
            partInfo.setFileName(null);

            LOG.debug("Finished saving outgoing payload [{}] to database", partInfo.getHref());

            backendNotificationService.notifyPayloadProcessed(userMessage, originalFileName, partInfo, backendName);
        }
    }

    protected long saveIncomingFileToDisk(File file, InputStream is) throws IOException {
        try (OutputStream fileOutputStream = new FileOutputStream(file)) {
            final long total = IOUtils.copy(is, fileOutputStream, DEFAULT_BUFFER_SIZE);
            fileOutputStream.flush();
            LOG.debug("Done writing file [{}]. Written [{}] bytes.", file.getName(), total);
            return total;
        }
    }

    protected void setContentType(PartInfo partInfo) {
        String contentType = partInfo.getPayloadDatahandler().getContentType();
        if (StringUtils.isBlank(contentType)) {
            contentType = MIME_TYPE_APPLICATION_UNKNOWN;
        }
        LOG.debug("Setting the payload [{}] content type to [{}]", partInfo.getHref(), contentType);
        partInfo.setMime(contentType);
    }

    protected byte[] getOutgoingBinaryData(PartInfo partInfo, InputStream is, UserMessage userMessage, final LegConfiguration legConfiguration) throws IOException, EbMS3Exception {
        byte[] binaryData = IOUtils.toByteArray(is);

        boolean useCompression = compressionService.handleCompression(userMessage.getMessageInfo().getMessageId(), partInfo, legConfiguration);
        LOG.debug("Compression for message with id: [{}] applied: [{}]", userMessage.getMessageInfo().getMessageId(), useCompression);

        if (useCompression) {
            binaryData = compress(binaryData);
        }

        return binaryData;
    }

    protected long saveOutgoingFileToDisk(File file, PartInfo partInfo, InputStream is, UserMessage userMessage, final LegConfiguration legConfiguration) throws IOException, EbMS3Exception {
        boolean useCompression = compressionService.handleCompression(userMessage.getMessageInfo().getMessageId(), partInfo, legConfiguration);
        LOG.debug("Compression for message with id: [{}] applied: [{}]", userMessage.getMessageInfo().getMessageId(), useCompression);

        OutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            if (useCompression) {
                LOG.debug("Using compression for storing the file [{}]", file);
                fileOutputStream = new GZIPOutputStream(fileOutputStream);
            }

            final long total = IOUtils.copy(is, fileOutputStream, MessagingServiceImpl.DEFAULT_BUFFER_SIZE);
            LOG.debug("Done writing file [{}]. Written [{}] bytes.", file.getName(), total);
            return total;
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }

    }

    protected byte[] compress(byte[] binaryData) throws IOException {
        LOG.debug("Compressing binary data");
        final byte[] buffer = new byte[MessagingServiceImpl.DEFAULT_BUFFER_SIZE];
        InputStream sourceStream = new ByteArrayInputStream(binaryData);
        ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
        GZIPOutputStream targetStream = new GZIPOutputStream(compressedContent);
        int i;
        while ((i = sourceStream.read(buffer)) > 0) {
            targetStream.write(buffer, 0, i);
        }
        sourceStream.close();
        targetStream.finish();
        targetStream.close();

        return compressedContent.toByteArray();
    }

    protected boolean hasCompressionProperty(PartInfo partInfo) {
        if (partInfo.getPartProperties() == null) {
            return false;
        }

        for (final Property property : partInfo.getPartProperties().getProperties()) {
            if (property.getName().equalsIgnoreCase(CompressionService.COMPRESSION_PROPERTY_KEY)
                    && property.getValue().equalsIgnoreCase(CompressionService.COMPRESSION_PROPERTY_VALUE)) {
                return true;
            }
        }

        return false;
    }
}
