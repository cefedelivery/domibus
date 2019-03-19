package eu.domibus.core.message.fragment;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.messaging.MessagingException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Splitting;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.configuration.storage.StorageProvider;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.handler.DatabaseMessageHandler;
import eu.domibus.plugin.transformer.impl.UserMessageFactory;
import eu.domibus.util.MessageUtil;
import eu.domibus.util.SoapUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.attachment.AttachmentDeserializer;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class SplitAndJoinDefaultService implements SplitAndJoinService {

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SplitAndJoinDefaultService.class);

    @Autowired
    protected MessagingDao messagingDao;

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected SoapUtil soapUtil;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected StorageProvider storageProvider;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected UserMessageFactory userMessageFactory;

    @Autowired
    private DatabaseMessageHandler databaseMessageHandler;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @Override
    public boolean mayUseSplitAndJoin(LegConfiguration legConfiguration) {
        final Splitting splitting = legConfiguration.getSplitting();
        if (splitting == null) {
            return false;
        }
        return true;
    }

    @Override
    public String generateSourceFileName(String temporaryDirectoryLocation) {
        final String uuid = UUID.randomUUID().toString();
        return temporaryDirectoryLocation + "/" + uuid;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200) // 20 minutes
    @Override
    public void createMessageFragments(UserMessage sourceMessage, MessageGroupEntity messageGroupEntity, List<String> fragmentFiles) {
        messageGroupDao.create(messageGroupEntity);

        String backendName = userMessageLogDao.findBackendForMessageId(sourceMessage.getMessageInfo().getMessageId());
        for (int index = 0; index < fragmentFiles.size(); index++) {
            try {
                final String fragmentFile = fragmentFiles.get(index);
                createMessagingForFragment(sourceMessage, messageGroupEntity, backendName, fragmentFile, index + 1);
            } catch (MessagingProcessingException e) {
                LOG.error("Could not create Messaging for fragment [{}]", index);
                throw new WebServiceException(e);
            }
        }
    }

    protected void createMessagingForFragment(UserMessage userMessage, MessageGroupEntity messageGroupEntity, String backendName, String fragmentFile, int index) throws MessagingProcessingException {
        final UserMessage userMessageFragment = userMessageFactory.createUserMessageFragment(userMessage, messageGroupEntity, Long.valueOf(index), fragmentFile);
        databaseMessageHandler.submitMessageFragment(userMessageFragment, backendName);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public SOAPMessage rejoinSourceMessage(String groupId) {
        LOG.debug("Rejoining the SourceMessage for group [{}]", groupId);

        final List<UserMessage> userMessageFragments = messagingDao.findUserMessageByGroupId(groupId);
        final MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(groupId);
        if (messageGroupEntity.getFragmentCount() != userMessageFragments.size()) {
            throw new MessagingException(DomibusCoreErrorCode.DOM_001, "Could not rejoin fragments: number of fragments found do not correspond with the total fragment count", null);
        }

        List<File> fragmentFilesInOrder = new ArrayList<>();
        for (UserMessage userMessage : userMessageFragments) {
            final PartInfo partInfo = userMessage.getPayloadInfo().getPartInfo().iterator().next();
            final String fileName = partInfo.getFileName();
            if (StringUtils.isBlank(fileName)) {
                throw new MessagingException(DomibusCoreErrorCode.DOM_001, "Could not rejoin fragments: filename is null for part [" + partInfo.getHref() + "]", null);
            }
            fragmentFilesInOrder.add(new File(fileName));
        }

        final File sourceMessageFile = mergeSourceFile(fragmentFilesInOrder, messageGroupEntity);
        LOG.debug("Rejoined the SourceMessage for group [{}] into file [{}] of length [{}]", groupId, sourceMessageFile, sourceMessageFile.length());

        LOG.debug("Creating the SOAPMessage from file [{}]", sourceMessageFile);
        try (InputStream rawInputStream = new FileInputStream(sourceMessageFile)) {
            MessageImpl messageImpl = new MessageImpl();
            messageImpl.setContent(InputStream.class, rawInputStream);
            messageImpl.put(Message.CONTENT_TYPE, createContentType(messageGroupEntity.getMessageHeaderEntity().getBoundary(), messageGroupEntity.getMessageHeaderEntity().getStart()));
            new AttachmentDeserializer(messageImpl).initializeAttachments();
            final SOAPMessage soapMessage = soapUtil.createUserMessage(messageImpl);
            LOG.debug("Created the SOAPMessage from file [{}]", sourceMessageFile);
            return soapMessage;
        } catch (Exception e) {
            //TODO return a signal error to C2 and notify the backend
            LOG.error("Error parsing the source file [{}]", sourceMessageFile);
            return null;
        }
    }

    protected String createContentType(String boundary, String start) {
        final String contentType = "multipart/related; type=\"application/soap+xml\"; boundary=" + boundary + "; start=" + start + "; start-info=\"application/soap+xml\"";
        LOG.debug("Created contentType [{}]", contentType);
        return contentType;
    }

    protected File mergeSourceFile(List<File> fragmentFilesInOrder, MessageGroupEntity messageGroupEntity) {


        final String temporaryDirectoryLocation = domibusPropertyProvider.getProperty(Storage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION);
        if (StringUtils.isEmpty(temporaryDirectoryLocation)) {
            throw new MessagingException(DomibusCoreErrorCode.DOM_001, "Could not rejoin fragments: the property [" + Storage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION + "] is not defined", null);
        }
        String sourceFileName = generateSourceFileName(temporaryDirectoryLocation);
        String outputFileName = sourceFileName;
        final boolean sourceMessageCompressed = isSourceMessageCompressed(messageGroupEntity);
        if (sourceMessageCompressed) {
            outputFileName = sourceFileName + "_compressed";
        }

        final File outputFile = new File(outputFileName);

        LOG.debug("Merging files [{}] for group [{}] into file [{}]", fragmentFilesInOrder, messageGroupEntity.getGroupId(), outputFile);

        try (OutputStream mergingStream = new FileOutputStream(outputFile)) {
            mergeFiles(fragmentFilesInOrder, mergingStream);
        } catch (IOException exp) {
            throw new MessagingException(DomibusCoreErrorCode.DOM_001, "Could not rejoin fragments", exp);
        }
        if (!sourceMessageCompressed) {
            return outputFile;
        }


        final File decompressedSourceFile = new File(sourceFileName);
        try {
            LOG.debug("Decompressing SourceMessage file [{}] into file [{}]", outputFile, decompressedSourceFile);
            decompressGzip(outputFile, decompressedSourceFile);
            LOG.debug("Deleting file [{}]", outputFile);
            final boolean delete = outputFile.delete();
            if (!delete) {
                LOG.warn("Could not delete file [{}]", outputFile);
            }
        } catch (IOException exp) {
            throw new MessagingException(DomibusCoreErrorCode.DOM_001, "Could not rejoin fragments", exp);
        }
        return decompressedSourceFile;
    }

    protected boolean isSourceMessageCompressed(MessageGroupEntity messageGroupEntity) {
        return StringUtils.isNotBlank(messageGroupEntity.getCompressionAlgorithm());
    }

    protected void mergeFiles(List<File> files, OutputStream mergingStream) throws IOException {
        for (File f : files) {
            Files.copy(f.toPath(), mergingStream);
            mergingStream.flush();
        }
    }

    protected void decompressGzip(File input, File output) throws IOException {
        try (GZIPInputStream in = new GZIPInputStream(new FileInputStream(input))) {
            try (FileOutputStream out = new FileOutputStream(output)) {
                IOUtils.copy(in, out, 32 * 1024);
            }
        }
    }
}
