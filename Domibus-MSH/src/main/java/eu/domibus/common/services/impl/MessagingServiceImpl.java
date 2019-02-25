package eu.domibus.common.services.impl;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.MessagingService;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.configuration.storage.StorageProvider;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * @author Ioana Dragusanu
 * @since 3.3
 */
@Service
public class MessagingServiceImpl implements MessagingService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagingServiceImpl.class);
    public static final String PAYLOAD_EXTENSION = ".payload";
    public static final String MIME_TYPE_APPLICATION_UNKNOWN = "application/unknown";

    @Autowired
    MessagingDao messagingDao;

    @Autowired
    StorageProvider storageProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    SplitAndJoinService splitAndJoinService;

    @Autowired
    private CompressionService compressionService;

    @Override
    public void storeMessage(Messaging messaging, MSHRole mshRole, final LegConfiguration legConfiguration) throws CompressionException {
        if (messaging == null || messaging.getUserMessage() == null)
            return;

        if (messaging.getUserMessage().getPayloadInfo() != null && messaging.getUserMessage().getPayloadInfo().getPartInfo() != null) {
            for (PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
                try {
                    if (MSHRole.RECEIVING.equals(mshRole)) {
                        storeIncomingPayload(partInfo, messaging.getUserMessage().getMessageInfo().getMessageId());
                    } else {
                        storeOutgoingPayload(partInfo, messaging.getUserMessage(), legConfiguration);
                    }
                } catch (IOException | EbMS3Exception exc) {
                    LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, partInfo.getHref());
                    throw new CompressionException("Could not store binary data for message " + exc.getMessage(), exc);
                }
            }
        }

        messagingDao.create(messaging);
    }

    protected void storeIncomingPayload(PartInfo partInfo, String messageId) throws IOException {
        setContentType(partInfo);

        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        Storage currentStorage = storageProvider.forDomain(currentDomain);
        LOG.debug("Retrieved Storage for domain [{}]", currentDomain);
        if (currentStorage == null) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not retrieve Storage for domain" + currentDomain + " is null");
        }

        if (savePayloadsInDatabase(currentStorage)) {
            try (InputStream is = partInfo.getPayloadDatahandler().getInputStream()) {
                byte[] binaryData = IOUtils.toByteArray(is);
                partInfo.setBinaryData(binaryData);
                partInfo.setLength(binaryData.length);
                partInfo.setFileName(null);
            }
        } else {
            final File attachmentStore = new File(currentStorage.getStorageDirectory(), UUID.randomUUID().toString() + ".payload");
            partInfo.setFileName(attachmentStore.getAbsolutePath());
            try (final InputStream inputStream = partInfo.getPayloadDatahandler().getInputStream()) {
                final long fileLength = saveIncomingFileToDisk(attachmentStore, inputStream);
                partInfo.setLength(fileLength);
            }
        }


        // Log Payload size
        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIVED_PAYLOAD_SIZE, partInfo.getHref(), messageId, partInfo.getLength());
    }


    protected void storeOutgoingPayload(PartInfo partInfo, UserMessage userMessage, final LegConfiguration legConfiguration) throws IOException, EbMS3Exception {
        String messageId = userMessage.getMessageInfo().getMessageId();

        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        Storage currentStorage = storageProvider.forDomain(currentDomain);
        LOG.debug("Retrieved Storage for domain [{}]", currentDomain);
        if (currentStorage == null) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not retrieve Storage for domain" + currentDomain + " is null");
        }

        if (savePayloadsInDatabase(currentStorage)) {
            InputStream is = partInfo.getPayloadDatahandler().getInputStream();
            byte[] binaryData = getOutgoingBinaryData(partInfo, is, userMessage, legConfiguration);
            partInfo.setBinaryData(binaryData);
            partInfo.setLength(binaryData.length);
            partInfo.setFileName(null);
        } else {
            final boolean mayUseSplitAndJoin = splitAndJoinService.mayUseSplitAndJoin(legConfiguration);
            userMessage.setSplitAndJoin(mayUseSplitAndJoin);
            if (StringUtils.isBlank(partInfo.getFileName())) {

                InputStream is = partInfo.getPayloadDatahandler().getInputStream();
                final File attachmentStore = new File(currentStorage.getStorageDirectory(), UUID.randomUUID().toString() + PAYLOAD_EXTENSION);
                partInfo.setFileName(attachmentStore.getAbsolutePath());
                final long fileLength = saveOutgoingFileToDisk(attachmentStore, partInfo, is, userMessage, legConfiguration);
                partInfo.setLength(fileLength);
            }
        }

        setContentType(partInfo);

        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SENDING_PAYLOAD_SIZE, partInfo.getHref(), messageId, partInfo.getLength());

        final boolean hasCompressionProperty = hasCompressionProperty(partInfo);
        if (hasCompressionProperty) {
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION, partInfo.getHref());
        }
    }

    protected boolean savePayloadsInDatabase(Storage currentStorage) {
        return currentStorage.getStorageDirectory() == null || currentStorage.getStorageDirectory().getName() == null;
    }

    protected void setContentType(PartInfo partInfo) {
        String contentType = partInfo.getPayloadDatahandler().getContentType();
        if (StringUtils.isBlank(contentType)) {
            contentType = MIME_TYPE_APPLICATION_UNKNOWN;
        }
        LOG.debug("Setting the payload [{}] content type to [{}]", partInfo.getHref(), contentType);
        partInfo.setMime(contentType);
    }

    protected long saveIncomingFileToDisk(File file, InputStream is) throws IOException {
        try (OutputStream fileOutputStream = new FileOutputStream(file)) {
            final long total = IOUtils.copyLarge(is, fileOutputStream);
            fileOutputStream.flush();
            LOG.debug("Done writing file [{}]. Written [{}] bytes.", file.getName(), total);
            return total;
        }
    }

    protected byte[] getOutgoingBinaryData(PartInfo partInfo, InputStream is, UserMessage userMessage, final LegConfiguration legConfiguration) throws IOException, EbMS3Exception {
        byte[] binaryData = IOUtils.toByteArray(is);

        final boolean mayUseSplitAndJoin = splitAndJoinService.mayUseSplitAndJoin(legConfiguration);
        if (!mayUseSplitAndJoin) {
            boolean useCompression = compressionService.handleCompression(userMessage.getMessageInfo().getMessageId(), partInfo, legConfiguration);
            LOG.debug("Compression for message with id: [{}] applied: [{}]", userMessage.getMessageInfo().getMessageId(), useCompression);

            if (useCompression) {
                binaryData = compress(binaryData);
            }
        } else {
            userMessage.setSplitAndJoin(true);
        }

        return binaryData;
    }

    protected long saveOutgoingFileToDisk(File file, PartInfo partInfo, InputStream is, UserMessage userMessage, final LegConfiguration legConfiguration) throws IOException, EbMS3Exception {
        OutputStream fileOutputStream = new FileOutputStream(file);

        boolean useCompression = compressionService.handleCompression(userMessage.getMessageInfo().getMessageId(), partInfo, legConfiguration);
        LOG.debug("Compression for message with id: [{}] applied: [{}]", userMessage.getMessageInfo().getMessageId(), useCompression);

        if (useCompression) {
            LOG.debug("Using compression for storing the file [{}]", file);
            fileOutputStream = new GZIPOutputStream(fileOutputStream);
        }

        final long total = IOUtils.copyLarge(is, fileOutputStream);
        fileOutputStream.flush();
        IOUtils.closeQuietly(fileOutputStream);
        LOG.debug("Done writing file [{}]. Written [{}] bytes.", file.getName(), total);
        return total;
    }

    protected byte[] compress(byte[] binaryData) throws IOException {
        LOG.debug("Compressing binary data");
        final byte[] buffer = new byte[1024];
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
