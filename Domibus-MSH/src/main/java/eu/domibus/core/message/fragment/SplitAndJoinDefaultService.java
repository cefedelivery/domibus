package eu.domibus.core.message.fragment;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.UserMessageService;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Splitting;
import eu.domibus.common.model.logging.UserMessageLog;
import eu.domibus.common.services.MessagingService;
import eu.domibus.common.services.impl.AS4ReceiptService;
import eu.domibus.common.services.impl.MessageRetentionService;
import eu.domibus.common.services.impl.UserMessageHandlerService;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.configuration.storage.StorageProvider;
import eu.domibus.core.message.UserMessageDefaultService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.AttachmentCleanupService;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.receiver.handler.IncomingSourceMessageHandler;
import eu.domibus.ebms3.sender.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.PolicyService;
import eu.domibus.util.MessageUtil;
import eu.domibus.util.SoapUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.attachment.AttachmentDeserializer;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.http.entity.ContentType;
import org.apache.neethi.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@Service
public class SplitAndJoinDefaultService implements SplitAndJoinService {

    private static final Long MB_IN_BYTES = 1048576L;
    public static final String BOUNDARY = "boundary";
    public static final String START = "start";
    public static final String FRAGMENT_FILENAME_SEPARATOR = "_";

    public static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SplitAndJoinDefaultService.class);

    @Autowired
    protected DomainContextProvider domainContextProvider;

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
    protected UserMessageDefaultService userMessageDefaultService;

    @Autowired
    protected UserMessageLogDao userMessageLogDao;

    @Autowired
    protected UpdateRetryLoggingService updateRetryLoggingService;

    @Autowired
    protected AttachmentCleanupService attachmentCleanupService;

    @Autowired
    protected UserMessageHandlerService userMessageHandlerService;

    @Autowired
    protected MessagingService messagingService;

    @Autowired
    protected UserMessageService userMessageService;

    @Autowired
    protected IncomingSourceMessageHandler incomingSourceMessageHandler;

    @Autowired
    protected PolicyService policyService;

    @Autowired
    protected MSHDispatcher mshDispatcher;

    @Autowired
    protected AS4ReceiptService as4ReceiptService;

    @Autowired
    protected EbMS3MessageBuilder messageBuilder;

    @Autowired
    protected MessageRetentionService messageRetentionService;

    @Autowired
    protected MessageGroupService messageGroupService;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public void createUserFragmentsFromSourceFile(String sourceMessageFileName, SOAPMessage sourceMessageRequest, UserMessage userMessage, String contentTypeString, boolean compression) {
        MessageGroupEntity messageGroupEntity = new MessageGroupEntity();
        messageGroupEntity.setGroupId(userMessage.getMessageInfo().getMessageId());
        File sourceMessageFile = new File(sourceMessageFileName);
        messageGroupEntity.setMessageSize(BigInteger.valueOf(sourceMessageFile.length()));
        if (compression) {
            final File compressSourceMessage = compressSourceMessage(sourceMessageFileName);
            LOG.debug("Deleting file [{}]", sourceMessageFile);
            final boolean sourceDeleteSuccessful = sourceMessageFile.delete();
            if (!sourceDeleteSuccessful) {
                LOG.warn("Could not delete uncompressed source file [{}]", sourceMessageFile);
            }

            LOG.debug("Using [{}] as source message file ", compressSourceMessage);
            sourceMessageFile = compressSourceMessage;
            messageGroupEntity.setCompressedMessageSize(BigInteger.valueOf(compressSourceMessage.length()));
            messageGroupEntity.setCompressionAlgorithm("application/gzip");
        }

        messageGroupEntity.setSoapAction(StringUtils.EMPTY);
        messageGroupEntity.setSourceMessageId(userMessage.getMessageInfo().getMessageId());

        MessageExchangeConfiguration userMessageExchangeConfiguration = null;
        LegConfiguration legConfiguration = null;
        try {
            userMessageExchangeConfiguration = pModeProvider.findUserMessageExchangeContext(userMessage, MSHRole.SENDING);
            String pModeKey = userMessageExchangeConfiguration.getPmodeKey();
            legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
        } catch (EbMS3Exception e) {
            throw new SplitAndJoinException("Could not get LegConfiguration", e);
        }

        List<String> fragmentFiles = null;
        try {
            fragmentFiles = splitSourceMessage(sourceMessageFile, legConfiguration.getSplitting().getFragmentSize());
        } catch (IOException e) {
            throw new SplitAndJoinException("Could not split SourceMessage " + sourceMessageFileName, e);
        }
        messageGroupEntity.setFragmentCount(Long.valueOf(fragmentFiles.size()));
        LOG.debug("Deleting source file [{}]", sourceMessageFile);
        final boolean deleteSuccessful = sourceMessageFile.delete();
        if (!deleteSuccessful) {
            LOG.warn("Could not delete source file [{}]", sourceMessageFile);
        }
        LOG.debug("Finished deleting source file [{}]", sourceMessageFile);

        final ContentType contentType = ContentType.parse(contentTypeString);
        MessageHeaderEntity messageHeaderEntity = new MessageHeaderEntity();
        messageHeaderEntity.setBoundary(contentType.getParameter(BOUNDARY));
        final String start = contentType.getParameter(START);
        messageHeaderEntity.setStart(StringUtils.replaceEach(start, new String[]{"<", ">"}, new String[]{"", ""}));
        messageGroupEntity.setMessageHeaderEntity(messageHeaderEntity);

        userMessageDefaultService.createMessageFragments(userMessage, messageGroupEntity, fragmentFiles);

        attachmentCleanupService.cleanAttachments(sourceMessageRequest);

        LOG.debug("Finished processing source message file");
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void rejoinSourceMessage(String groupId, String sourceMessageFile, String backendName) {
        LOG.debug("Rejoining SourceMessage for group [{}]", groupId);

        final SOAPMessage sourceRequest = rejoinSourceMessage(groupId, new File(sourceMessageFile));
        Messaging sourceMessaging = messageUtil.getMessage(sourceRequest);

        MessageExchangeConfiguration userMessageExchangeContext = null;
        LegConfiguration legConfiguration = null;
        try {
            userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(sourceMessaging.getUserMessage(), MSHRole.RECEIVING);
            String sourcePmodeKey = userMessageExchangeContext.getPmodeKey();
            legConfiguration = pModeProvider.getLegConfiguration(sourcePmodeKey);
            sourceRequest.setProperty(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY, sourcePmodeKey);
        } catch (EbMS3Exception | SOAPException e) {
            throw new SplitAndJoinException("Error getting the pmodeKey", e);
        }

        try {
            userMessageHandlerService.handlePayloads(sourceRequest, sourceMessaging.getUserMessage());
        } catch (EbMS3Exception | SOAPException | TransformerException e) {
            throw new SplitAndJoinException("Error handling payloads", e);
        }

        messagingService.storePayloads(sourceMessaging, MSHRole.RECEIVING, legConfiguration, backendName);

        final String sourceMessageId = sourceMessaging.getUserMessage().getMessageInfo().getMessageId();
        messageGroupService.setSourceMessageId(sourceMessageId, groupId);

        incomingSourceMessageHandler.processMessage(sourceRequest, sourceMessaging);
        userMessageService.scheduleSourceMessageReceipt(sourceMessageId, userMessageExchangeContext.getReversePmodeKey());

        LOG.debug("Finished rejoining SourceMessage for group [{}]", groupId);
    }

    @Override
    public void sendSourceMessageReceipt(String sourceMessageId, String pModeKey) {
        SOAPMessage receiptMessage = null;
        try {
            receiptMessage = as4ReceiptService.generateReceipt(sourceMessageId, false);
        } catch (EbMS3Exception e) {
            throw new SplitAndJoinException("Error generating the source message receipt", e);
        }
        sendSignalMessage(receiptMessage, pModeKey);

    }

    @Override
    public void sendSignalError(String messageId, String ebMS3ErrorCode, String errorDetail, String pmodeKey) {
        final ErrorCode.EbMS3ErrorCode errorCode = ErrorCode.EbMS3ErrorCode.findErrorCodeBy(ebMS3ErrorCode);

        EbMS3Exception ebMS3Exception = new EbMS3Exception(errorCode, errorDetail, messageId, null);
        ebMS3Exception.setMshRole(MSHRole.RECEIVING);
        SOAPMessage soapMessage = null;
        try {
            soapMessage = messageBuilder.buildSOAPFaultMessage(ebMS3Exception.getFaultInfoError());
        } catch (EbMS3Exception e) {
            throw new SplitAndJoinException("Error generating the Signal SOAPMessage for SourceMessage [" + messageId + "]", e);
        }
        sendSignalMessage(soapMessage, pmodeKey);
    }

    protected void sendSignalMessage(SOAPMessage soapMessage, String pModeKey) {
        final LegConfiguration legConfiguration = pModeProvider.getLegConfiguration(pModeKey);
        final Party receiverParty = pModeProvider.getReceiverParty(pModeKey);
        final Policy policy = policyService.getPolicy(legConfiguration);

        try {
            mshDispatcher.dispatch(soapMessage, receiverParty.getEndpoint(), policy, legConfiguration, pModeKey);
        } catch (EbMS3Exception e) {
            throw new SplitAndJoinException("Error dispatching SourceMessage receipt", e);
        }
    }

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

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public File rejoinMessageFragments(String groupId) {
        LOG.debug("Rejoining the SourceMessage for group [{}]", groupId);

        final MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(groupId);
        if (messageGroupEntity == null) {
            throw new SplitAndJoinException("Could not rejoin fragments: could not find group [" + groupId + "]");
        }

        final List<UserMessage> userMessageFragments = messagingDao.findUserMessageByGroupId(groupId);

        if (messageGroupEntity.getFragmentCount() != userMessageFragments.size()) {
            throw new SplitAndJoinException("Could not rejoin fragments: number of fragments found [" + userMessageFragments.size() + "] do not correspond with the total fragment count [" + messageGroupEntity.getFragmentCount() + "]");
        }

        List<File> fragmentFilesInOrder = new ArrayList<>();
        for (UserMessage userMessage : userMessageFragments) {
            final PartInfo partInfo = userMessage.getPayloadInfo().getPartInfo().iterator().next();
            final String fileName = partInfo.getFileName();
            if (StringUtils.isBlank(fileName)) {
                throw new SplitAndJoinException("Could not rejoin fragments: filename is null for part [" + partInfo.getHref() + "]");
            }
            fragmentFilesInOrder.add(new File(fileName));
        }

        final File sourceMessageFile = mergeSourceFile(fragmentFilesInOrder, messageGroupEntity);
        LOG.debug("Rejoined the SourceMessage for group [{}] into file [{}] of length [{}]", groupId, sourceMessageFile, sourceMessageFile.length());

        return sourceMessageFile;
    }

    protected SOAPMessage rejoinSourceMessage(String groupId, File sourceMessageFile) {
        LOG.debug("Creating the SOAPMessage for group [{}] from file [{}] ", groupId, sourceMessageFile);

        final MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(groupId);
        final String contentType = createContentType(messageGroupEntity.getMessageHeaderEntity().getBoundary(), messageGroupEntity.getMessageHeaderEntity().getStart());

        return getUserMessage(sourceMessageFile, contentType);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public SOAPMessage getUserMessage(File sourceMessageFileName, String contentTypeString) {
        LOG.debug("Parsing the SOAPMessage from file [{}]", sourceMessageFileName);

        try (InputStream rawInputStream = new FileInputStream(sourceMessageFileName)) {
            MessageImpl messageImpl = new MessageImpl();
            final String temporaryDirectoryLocation = domibusPropertyProvider.getProperty(Storage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION);
            LOG.debug("Using temporaryDirectoryLocation for attachments [{}]", temporaryDirectoryLocation);
            messageImpl.put(AttachmentDeserializer.ATTACHMENT_DIRECTORY, temporaryDirectoryLocation);
            messageImpl.setContent(InputStream.class, rawInputStream);
            messageImpl.put(Message.CONTENT_TYPE, contentTypeString);

            LOG.debug("Start initializeAttachments");
            new AttachmentDeserializer(messageImpl).initializeAttachments();
            LOG.debug("End initializeAttachments");

            LOG.debug("Start createUserMessage");
            final SOAPMessage soapMessage = soapUtil.createUserMessage(messageImpl);
            LOG.debug("End createUserMessage");

            return soapMessage;
        } catch (Exception e) {
            throw new SplitAndJoinException(e);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public void setSourceMessageAsFailed(UserMessage userMessage) {
        final String messageId = userMessage.getMessageInfo().getMessageId();
        LOG.debug("Setting the SourceMessage [{}] as failed", messageId);

        final UserMessageLog messageLog = userMessageLogDao.findByMessageIdSafely(messageId);
        if (messageLog == null) {
            LOG.error("UserMessageLogEntity not found for message [{}]: could not mark the message as failed", messageId);
            return;
        }
        updateRetryLoggingService.messageFailedInANewTransaction(userMessage, messageLog);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public void setUserMessageFragmentAsFailed(String messageId) {
        LOG.debug("Setting the UserMessage fragment [{}] as failed", messageId);

        final UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        if (userMessage == null) {
            LOG.error("UserMessage not found for message [{}]: could not mark the message as failed", messageId);
            return;
        }

        final UserMessageLog messageLog = userMessageLogDao.findByMessageIdSafely(messageId);
        if (messageLog == null) {
            LOG.error("UserMessageLogEntity not found for message [{}]: could not mark the message as failed", messageId);
            return;
        }
        updateRetryLoggingService.messageFailed(userMessage, messageLog);
    }

    @Override
    public void splitAndJoinSendFailed(String groupId) {
        LOG.debug("SplitAndJoin sending failed for group [{}]", groupId);

        final MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(groupId);
        if (messageGroupEntity == null) {
            LOG.warn("Group not found [{}]: could't clear SplitAndJoin messages for group", groupId);
            return;
        }

        LOG.debug("Marking the group [{}] as rejected", groupId);
        messageGroupEntity.setRejected(true);
        messageGroupDao.update(messageGroupEntity);

        final UserMessage sourceUserMessage = messagingDao.findUserMessageByMessageId(messageGroupEntity.getSourceMessageId());
        setSourceMessageAsFailed(sourceUserMessage);

        final List<UserMessage> groupUserMessages = messagingDao.findUserMessageByGroupId(groupId);
        groupUserMessages.stream().forEach(userMessage -> userMessageService.scheduleUserMessageFragmentFailed(userMessage.getMessageInfo().getMessageId()));
    }

    @Transactional
    @Override
    public void splitAndJoinReceiveFailed(final String groupId, final String sourceMessageId, final String errorDetail) {
        LOG.debug("SplitAndJoin receiving failed for group [{}]", groupId);

        final MessageGroupEntity messageGroupEntity = messageGroupDao.findByGroupId(groupId);
        if (messageGroupEntity == null) {
            LOG.warn("Group not found [{}]: could not handle splitAndJoinReceiveFailed", groupId);
            return;
        }

        LOG.debug("Marking the group [{}] as rejected", groupId);
        messageGroupEntity.setRejected(true);
        messageGroupDao.update(messageGroupEntity);

        final List<UserMessage> userMessageFragments = messagingDao.findUserMessageByGroupId(groupId);
        if (userMessageFragments == null || userMessageFragments.isEmpty()) {
            throw new SplitAndJoinException("Error generating the Signal SOAPMessage for SourceMessage [" + sourceMessageId + "]: no message fragments found");
        }

        final List<String> userMessageIds = userMessageFragments.stream().map(userMessage -> userMessage.getMessageInfo().getMessageId()).collect(Collectors.toList());
        messageRetentionService.scheduleDeleteMessages(userMessageIds);

        if (StringUtils.isNotEmpty(sourceMessageId)) {
            LOG.debug("Scheduling sending the Signal error for SourceMessage [{}]", sourceMessageId);

            final UserMessage messageFragment = userMessageFragments.iterator().next();
            MessageExchangeConfiguration userMessageExchangeContext = null;
            try {
                userMessageExchangeContext = pModeProvider.findUserMessageExchangeContext(messageFragment, MSHRole.RECEIVING);
            } catch (EbMS3Exception e) {
                throw new SplitAndJoinException("Error generating the Signal SOAPMessage for SourceMessage [" + sourceMessageId + "]: could not get the MessageExchangeConfiguration", e);
            }
            userMessageDefaultService.scheduleSendingSignalError(sourceMessageId, ErrorCode.EbMS3ErrorCode.EBMS_0004.getCode().getErrorCode().getErrorCodeName(), errorDetail, userMessageExchangeContext.getReversePmodeKey());
        }
    }

    protected File compressSourceMessage(String fileName) {
        String compressedFileName = fileName + ".zip";
        LOG.debug("Compressing the source message file [{}] to [{}]", fileName, compressedFileName);
        try (GZIPOutputStream out = new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(compressedFileName)));
             FileInputStream sourceMessageInputStream = new FileInputStream(fileName)) {
            byte[] buffer = new byte[32 * 1024];
            int len;
            while ((len = sourceMessageInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            LOG.error("Could not compress the message content to file " + fileName);
            throw new Fault(e);
        }
        LOG.debug("Finished compressing the source message file [{}] to [{}]", fileName, compressedFileName);
        return new File(compressedFileName);
    }


    protected List<String> splitSourceMessage(File sourceMessageFile, int fragmentSizeInMB) throws IOException {
        LOG.debug("Source file [{}] will be split into fragments", sourceMessageFile);

        final long sourceSize = sourceMessageFile.length();
        long fragmentSizeInBytes = fragmentSizeInMB * MB_IN_BYTES;

        long bytesPerSplit;
        long fragmentCount = 1;
        long remainingBytes = 0;
        if (sourceSize > fragmentSizeInBytes) {
            fragmentCount = sourceSize / fragmentSizeInBytes;
            bytesPerSplit = fragmentSizeInBytes;

            if (fragmentCount > 0) {
                remainingBytes = sourceSize % (fragmentCount * fragmentSizeInBytes);
            }
        } else {
            bytesPerSplit = sourceSize;
        }
        final File storageDirectory = getFragmentStorageDirectory();
        return splitSourceFileIntoFragments(sourceMessageFile, storageDirectory, fragmentCount, bytesPerSplit, remainingBytes);
    }

    protected File getFragmentStorageDirectory() {
        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        Storage currentStorage = storageProvider.forDomain(currentDomain);
        LOG.debug("Retrieved Storage for domain [{}]", currentDomain);
        if (currentStorage == null) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not retrieve Storage for domain" + currentDomain + " is null");
        }
        if (currentStorage.getStorageDirectory() == null || currentStorage.getStorageDirectory().getName() == null) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not store fragment payload. Please configure " + Storage.ATTACHMENT_STORAGE_LOCATION + " when using SplitAndJoin");
        }
        return currentStorage.getStorageDirectory();
    }

    protected List<String> splitSourceFileIntoFragments(File sourceMessageFile, File storageDirectory, long fragmentCount, long bytesPerSplit, long remainingBytes) throws IOException {
        List<String> result = new ArrayList<>();

        LOG.debug("Splitting SourceMessage [{}] into [{}] fragments, bytesPerSplit [{}], remainingBytes [{}]", sourceMessageFile, fragmentCount, bytesPerSplit, remainingBytes);

        int maxReadBufferSize = 32 * 1024; //16KB
        try (RandomAccessFile raf = new RandomAccessFile(sourceMessageFile, "r")) {
            for (int index = 1; index <= fragmentCount; index++) {
                final String fragmentFileName = getFragmentFileName(storageDirectory, sourceMessageFile.getName(), index);
                result.add(fragmentFileName);
                saveFragmentPayload(bytesPerSplit, maxReadBufferSize, raf, fragmentFileName);
            }
            if (remainingBytes > 0) {
                final String remainingFragmentFileName = getFragmentFileName(storageDirectory, sourceMessageFile.getName(), (fragmentCount + 1));
                result.add(remainingFragmentFileName);

                try (final FileOutputStream outputStream = new FileOutputStream(remainingFragmentFileName);
                     final BufferedOutputStream bw = new BufferedOutputStream(outputStream)) {
                    readWrite(raf, bw, remainingBytes);
                }
            }
        }
        return result;
    }

    protected void saveFragmentPayload(long bytesPerSplit, int maxReadBufferSize, RandomAccessFile raf, final String fragmentFileName) throws IOException {
        LOG.debug("Saving fragment file [{}]", fragmentFileName);

        try (final FileOutputStream fileOutputStream = new FileOutputStream(fragmentFileName);
             BufferedOutputStream bw = new BufferedOutputStream(fileOutputStream)) {
            if (bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit / maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for (int index = 0; index < numReads; index++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if (numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            } else {
                readWrite(raf, bw, bytesPerSplit);
            }
        }
    }

    protected void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if (val != -1) {
            bw.write(buf);
        }
    }

    protected String getFragmentFileName(File outputDirectory, String sourceFileName, long fragmentNumber) {
        return outputDirectory.getAbsolutePath() + File.separator + sourceFileName + FRAGMENT_FILENAME_SEPARATOR + fragmentNumber;
    }

    protected String createContentType(String boundary, String start) {
        final String contentType = "multipart/related; type=\"application/soap+xml\"; boundary=" + boundary + "; start=" + start + "; start-info=\"application/soap+xml\"";
        LOG.debug("Created contentType [{}]", contentType);
        return contentType;
    }

    protected File mergeSourceFile(List<File> fragmentFilesInOrder, MessageGroupEntity messageGroupEntity) {
        final String temporaryDirectoryLocation = domibusPropertyProvider.getProperty(Storage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION);
        if (StringUtils.isEmpty(temporaryDirectoryLocation)) {
            throw new SplitAndJoinException("Could not rejoin fragments: the property [" + Storage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION + "] is not defined");
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
            throw new SplitAndJoinException("Could not rejoin fragments", exp);
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
            throw new SplitAndJoinException("Could not rejoin fragments", exp);
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
