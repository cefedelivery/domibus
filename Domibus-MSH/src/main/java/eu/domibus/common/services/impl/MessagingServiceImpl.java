package eu.domibus.common.services.impl;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.exception.CompressionException;
import eu.domibus.common.services.MessagingService;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.configuration.storage.StorageProvider;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.commons.io.IOUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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

    @Autowired
    MessagingDao messagingDao;

    private Storage storage;

    @Autowired
    StorageProvider storageProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void storeMessage(Messaging messaging, MSHRole mshRole) throws CompressionException {
        if (messaging == null || messaging.getUserMessage() == null)
            return;

        if (messaging.getUserMessage().getPayloadInfo() != null && messaging.getUserMessage().getPayloadInfo().getPartInfo() != null) {
            for (PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
                try {
                    storeBinary(partInfo, messaging.getUserMessage().getMessageInfo().getMessageId(), mshRole);
                } catch (IOException exc) {
                    LOG.businessError(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION_FAILURE, partInfo.getHref());
                    throw new CompressionException("Could not store binary data for message " + exc.getMessage(), exc);
                }
            }
        }

        messagingDao.create(messaging);
    }

    protected void storeBinary(PartInfo partInfo, String messageId, MSHRole mshRole) throws IOException {
        partInfo.setMime(partInfo.getPayloadDatahandler().getContentType());
        if (partInfo.getMime() == null) {
            partInfo.setMime("application/unknown");
        }
        InputStream is = partInfo.getPayloadDatahandler().getInputStream();
        final boolean compressed = isCompressed(partInfo);

        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        Storage currentStorage = this.storage == null ? storageProvider.forDomain(currentDomain) : this.storage;
        LOG.info("Retrieved Storage ben for domain [{}]", currentDomain);
        if(currentStorage == null)
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not retrieve Storage for domain" + currentDomain + " is null");

        if (currentStorage.getStorageDirectory() == null || currentStorage.getStorageDirectory().getName() == null) {
            byte[] binaryData = getBinaryData(is, compressed);
            partInfo.setBinaryData(binaryData);
            partInfo.setLength(binaryData.length);
            partInfo.setFileName(null);
        } else {
            final File attachmentStore = new File(currentStorage.getStorageDirectory(), UUID.randomUUID().toString() + ".payload");
            partInfo.setFileName(attachmentStore.getAbsolutePath());
            final long fileLength = saveFileToDisk(attachmentStore, is, compressed);
            partInfo.setLength(fileLength);
        }

        // Log Payload size
        if (MSHRole.RECEIVING.equals(mshRole)) {
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_RECEIVED_PAYLOAD_SIZE, partInfo.getHref(), messageId, partInfo.getLength());
        } else {
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_SENDING_PAYLOAD_SIZE, partInfo.getHref(), messageId, partInfo.getLength());
        }

        if (compressed) {
            LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_COMPRESSION, partInfo.getHref());
        }
    }

    protected byte[] getBinaryData(InputStream is, boolean isCompressed) throws IOException {
        byte[] binaryData = IOUtils.toByteArray(is);
        if (isCompressed) {
            binaryData = compress(binaryData);
        }
        return binaryData;
    }

    protected long saveFileToDisk(File file, InputStream is, boolean isCompressed) throws IOException {
        OutputStream fileOutputStream = new FileOutputStream(file);
        if (isCompressed) {
            fileOutputStream = new GZIPOutputStream(fileOutputStream);
        }
        final long total = IOUtils.copyLarge(is, fileOutputStream);
        fileOutputStream.flush();
        IOUtils.closeQuietly(fileOutputStream);
        LOG.debug("Done writing file [{}]. Written [{}] bytes.", file.getName(), total);
        return total;
    }

    protected byte[] compress(byte[] binaryData) throws IOException {
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

    protected boolean isCompressed(PartInfo partInfo) {
        if (partInfo.getPartProperties() != null) {
            for (final Property property : partInfo.getPartProperties().getProperties()) {
                if (property.getName().equals(CompressionService.COMPRESSION_PROPERTY_KEY) && property.getValue().equals(CompressionService.COMPRESSION_PROPERTY_VALUE)) {
                    return true;
                }
            }
        }

        return false;
    }
}
