package eu.domibus.common.services.impl;

import eu.domibus.common.DecompressionDataSource;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.CompressionMimeTypeBlacklist;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is responsible for compression handling of incoming and outgoing ebMS3 messages.
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
@Service
public class CompressionService {
    public static final String COMPRESSION_PROPERTY_KEY = "CompressionType";
    public static final String COMPRESSION_PROPERTY_VALUE = "application/gzip";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CompressionService.class);
    @Autowired
    private CompressionMimeTypeBlacklist blacklist;


    /**
     * This method is responsible for compression of payloads in a ebMS3 AS4 comformant way in case of {@link eu.domibus.common.MSHRole#SENDING}
     *
     * @param ebmsMessage         the sending {@link UserMessage} with all payloads
     * @param legConfigForMessage legconfiguration for this message
     * @return {@code true} if compression was applied properly and {@code false} if compression was not enabled in the corresponding pmode
     * @throws EbMS3Exception if an problem occurs during the compression or the mimetype was missing
     */
    public boolean handleCompression(final UserMessage ebmsMessage, final LegConfiguration legConfigForMessage) throws EbMS3Exception {
        //if compression is not necessary return false
        if (!legConfigForMessage.isCompressPayloads()) {
            return false;
        }

        for (final PartInfo partInfo : ebmsMessage.getPayloadInfo().getPartInfo()) {
            if (partInfo.isInBody()) {
                continue;
            }

            String mimeType = null;
            for (final Property property : partInfo.getPartProperties().getProperties()) {
                if (Property.MIME_TYPE.equals(property.getName())) {
                    mimeType = property.getValue();
                    break;
                }
            }

            if (mimeType == null || mimeType.isEmpty()) {
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, "No mime type found for payload with cid:" + partInfo.getHref(), ebmsMessage.getMessageInfo().getMessageId(), null);
                ex.setMshRole(MSHRole.SENDING);
                throw ex;
            }

            //if mimetype of payload is not considered to be compressed, skip
            if (this.blacklist.getEntries().contains(mimeType)) {
                continue;
            }

            final Property compressionProperty = new Property();
            compressionProperty.setName(CompressionService.COMPRESSION_PROPERTY_KEY);
            compressionProperty.setValue(CompressionService.COMPRESSION_PROPERTY_VALUE);
            partInfo.getPartProperties().getProperties().add(compressionProperty);
            DataHandler gZipDataHandler = new DataHandler(new CompressedDataSource(partInfo.getPayloadDatahandler().getDataSource()));
            partInfo.setPayloadDatahandler(gZipDataHandler);
            CompressionService.LOG.debug("Payload with cid: " + partInfo.getHref() + " and mime type: " + mimeType + " will be compressed");
        }

        return true;
    }

    /**
     * This method handles decompression of payloads for messages in case of {@link eu.domibus.common.MSHRole#RECEIVING}
     *
     * @param ebmsMessage the receving {@link UserMessage} with all payloads
     * @return {@code true} if everything was decompressed without problems, {@code false} in case of disabled compression via pmode
     * @throws EbMS3Exception if an problem occurs during the de compression or the mimetype of a compressed payload was missing
     */
    public boolean handleDecompression(final UserMessage ebmsMessage, final LegConfiguration legConfigForMessage) throws EbMS3Exception {
        //if compression is not necessary return false
        if (!legConfigForMessage.isCompressPayloads()) {
            return false;
        }

        for (final PartInfo partInfo : ebmsMessage.getPayloadInfo().getPartInfo()) {
            if (partInfo.isInBody()) {
                continue;
            }

            String mimeType = null;
            boolean payloadCompressed = false;

            for (final Property property : partInfo.getPartProperties().getProperties()) {
                if (Property.MIME_TYPE.equals(property.getName())) {
                    mimeType = property.getValue();
                }
                if (CompressionService.COMPRESSION_PROPERTY_KEY.equals(property.getName()) && CompressionService.COMPRESSION_PROPERTY_VALUE.equals(property.getValue())) {
                    payloadCompressed = true;
                }
            }

            if (!payloadCompressed) {
                continue;
            }

            final Property compressionProperty = new Property();
            compressionProperty.setName(CompressionService.COMPRESSION_PROPERTY_KEY);
            compressionProperty.setValue(CompressionService.COMPRESSION_PROPERTY_VALUE);
            partInfo.getPartProperties().getProperties().remove(compressionProperty);

            if (mimeType == null) {
                EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0303, "No mime type found for payload with cid:" + partInfo.getHref(), ebmsMessage.getMessageInfo().getMessageId(), null);
                ex.setMshRole(MSHRole.RECEIVING);
                throw ex;
            }
            partInfo.setPayloadDatahandler(new DataHandler(new DecompressionDataSource(partInfo.getPayloadDatahandler().getDataSource(), mimeType)));
            CompressionService.LOG.debug("Payload with cid: " + partInfo.getHref() + " and mime type: " + mimeType + " will be decompressed");
        }
        return true;
    }

    private class CompressedDataSource implements DataSource {
        private DataSource ds;

        private CompressedDataSource(DataSource ds) {
            this.ds = ds;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return ds.getInputStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return ds.getOutputStream();
        }

        @Override
        public String getContentType() {
            return CompressionService.COMPRESSION_PROPERTY_VALUE;
        }

        @Override
        public String getName() {
            return "compressed-" + ds.getName();
        }
    }
}
