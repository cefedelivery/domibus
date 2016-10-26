package eu.domibus.common.services.impl;

import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.services.IMessagingService;
import eu.domibus.configuration.Storage;
import eu.domibus.ebms3.common.model.CompressionService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.Property;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * Created by idragusa on 10/26/16.
 */
@Service
public class MessagingServiceImpl implements IMessagingService {

    private static final Log LOG = LogFactory.getLog(MessagingServiceImpl.class);

    @Autowired
    MessagingDao messagingDao;

    public void storeMessage(Messaging messaging) throws IOException {
        if (messaging == null || messaging.getUserMessage() == null)
            return;

        if (messaging.getUserMessage().getPayloadInfo() != null && messaging.getUserMessage().getPayloadInfo().getPartInfo() != null) {
            for (PartInfo partInfo : messaging.getUserMessage().getPayloadInfo().getPartInfo()) {
                storeBinary(partInfo);
            }
        }

        messagingDao.create(messaging);
    }

    protected void storeBinary(PartInfo partInfo) throws IOException {
        partInfo.setMime(partInfo.getPayloadDatahandler().getContentType());
        if (partInfo.getMime() == null) {
            partInfo.setMime("application/unknown");
        }
        if (Storage.storageDirectory == null) {

            byte[] binaryData = IOUtils.toByteArray(partInfo.getPayloadDatahandler().getInputStream());

            if (isCompressed(partInfo)) {
                final byte[] buffer = new byte[1024];
                InputStream sourceStream = new ByteArrayInputStream(binaryData);
                ByteArrayOutputStream compressedContent = new ByteArrayOutputStream();
                GZIPOutputStream targetStream = new GZIPOutputStream(compressedContent);
                try {
                    int i;
                    while ((i = sourceStream.read(buffer)) > 0) {
                        targetStream.write(buffer, 0, i);
                    }
                    sourceStream.close();
                    targetStream.finish();
                    targetStream.close();
                } catch (IOException e) {
                    LOG.error("I/O exception during gzip compression", e);
                    throw e;
                }
                binaryData = compressedContent.toByteArray();
            }

            partInfo.setBinaryData(binaryData);
            partInfo.setFileName(null);

        } else {
            final File attachmentStore = new File(Storage.storageDirectory, UUID.randomUUID().toString() + ".payload");
            partInfo.setFileName(attachmentStore.getAbsolutePath());
            OutputStream fileOutputStream = new FileOutputStream(attachmentStore);

            if (isCompressed(partInfo)) {
                fileOutputStream = new GZIPOutputStream(fileOutputStream);
            }

            IOUtils.copy(partInfo.getPayloadDatahandler().getInputStream(), fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        }
    }

    private boolean isCompressed(PartInfo partInfo) {
        for (final Property property : partInfo.getPartProperties().getProperties()) {
            if (property.getName().equals(CompressionService.COMPRESSION_PROPERTY_KEY) && property.getValue().equals(CompressionService.COMPRESSION_PROPERTY_VALUE)) {
                return true;
            }
        }
        return false;
    }

}
