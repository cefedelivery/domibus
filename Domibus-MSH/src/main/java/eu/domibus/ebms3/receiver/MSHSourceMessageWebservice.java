package eu.domibus.ebms3.receiver;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.services.impl.MessagingServiceImpl;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.configuration.storage.StorageProvider;
import eu.domibus.core.message.fragment.MessageGroupDao;
import eu.domibus.core.message.fragment.MessageGroupEntity;
import eu.domibus.core.message.fragment.MessageHeaderEntity;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.transformer.impl.UserMessageFactory;
import eu.domibus.util.MessageUtil;
import eu.domibus.util.SoapUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.attachment.AttachmentDeserializer;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;


/**
 * Local endpoint used to generate the multi mimepart message for SplitAndJoin
 */
@WebServiceProvider(portName = "local-msh-dispatch", serviceName = "local-msh-dispatch-service")
@ServiceMode(Service.Mode.MESSAGE)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class MSHSourceMessageWebservice implements Provider<SOAPMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHSourceMessageWebservice.class);

    public static final String SOURCE_MESSAGE_FILE = "sourceMessageFile";
    private static final Long MB_IN_BYTES = 1048576L;
    public static final String BOUNDARY = "boundary";
    public static final String START = "start";
    public static final String FRAGMENT_FILENAME_SEPARATOR = "_";

    @Autowired
    protected MessageFactory messageFactory;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Autowired
    protected StorageProvider storageProvider;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected SoapUtil soapUtil;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    protected UserMessageFactory userMessageFactory;

    @Autowired
    protected SplitAndJoinService splitAndJoinService;

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @WebMethod
    @WebResult(name = "soapMessageResult")
    @Transactional(propagation = Propagation.REQUIRED, timeout = 1200) // 20 minutes
    public SOAPMessage invoke(final SOAPMessage request) {
        LOG.debug("Processing SourceMessage request");

        final String domain = LOG.getMDC(MSHDispatcher.HEADER_DOMIBUS_DOMAIN);
        domainContextProvider.setCurrentDomain(domain);
        final Domain currentDomain = domainContextProvider.getCurrentDomain();

        final String contentTypeString = LOG.getMDC(Message.CONTENT_TYPE);
        final boolean compression = Boolean.valueOf(LOG.getMDC(MSHDispatcher.HEADER_DOMIBUS_SPLITTING_COMPRESSION));
        final String sourceMessageFileName = LOG.getMDC(MSHSourceMessageWebservice.SOURCE_MESSAGE_FILE);

        domainTaskExecutor.submitLongRunningTask(
                () -> {
                    UserMessage userMessage = getUserMessage(sourceMessageFileName, contentTypeString);

                    MessageGroupEntity messageGroupEntity = new MessageGroupEntity();
                    messageGroupEntity.setGroupId(UUID.randomUUID().toString());
                    File sourceMessageFile = new File(sourceMessageFileName);
                    messageGroupEntity.setMessageSize(BigInteger.valueOf(sourceMessageFile.length()));
                    if (compression) {
                        final File compressSourceMessage = compressSourceMessage(sourceMessageFileName);
                        LOG.debug("Deleting file [{}]", sourceMessageFile);
                        final boolean sourceDeleteSuccessful = sourceMessageFile.delete();
                        if(!sourceDeleteSuccessful) {
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
                        LOG.error("Could not get LegConfiguration", e);
                        throw new WebServiceException(e);
                    }

                    List<String> fragmentFiles = null;
                    try {
                        fragmentFiles = splitSourceMessage(sourceMessageFile, legConfiguration.getSplitting().getFragmentSize());
                    } catch (IOException e) {
                        LOG.error("Could not split source message", e);
                        throw new WebServiceException(e);
                    }
                    messageGroupEntity.setFragmentCount(Long.valueOf(fragmentFiles.size()));
                    LOG.debug("Deleting source file [{}]", sourceMessageFile);
                    final boolean deleteSuccessful = sourceMessageFile.delete();
                    if(!deleteSuccessful) {
                        LOG.warn("Could not delete source file [{}]", sourceMessageFile);
                    }
                    LOG.debug("Finished deleting source file [{}]", sourceMessageFile);

                    final ContentType contentType = ContentType.parse(contentTypeString);
                    MessageHeaderEntity messageHeaderEntity = new MessageHeaderEntity();
                    messageHeaderEntity.setBoundary(contentType.getParameter(BOUNDARY));
                    final String start = contentType.getParameter(START);
                    messageHeaderEntity.setStart(StringUtils.replaceEach(start, new String[]{"<", ">"}, new String[]{"", ""}));
                    messageGroupEntity.setMessageHeaderEntity(messageHeaderEntity);

                    splitAndJoinService.createMessageFragments(userMessage, messageGroupEntity, fragmentFiles);

                    LOG.debug("Finished processing source message file");
                },
                currentDomain,
                false,
                domibusPropertyProvider.getLongDomainProperty(currentDomain, MessagingServiceImpl.PROPERTY_WAIT_FOR_TASK), TimeUnit.MINUTES);

        try {
            SOAPMessage responseMessage = messageFactory.createMessage();
            responseMessage.saveChanges();

            LOG.debug("Finished processing SourceMessage request");
            return responseMessage;
        } catch (Exception e) {
            throw new WebServiceException(e);
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

    protected UserMessage getUserMessage(String sourceMessageFileName, String contentTypeString) {
        LOG.debug("Parsing UserMessage");
        try (InputStream rawInputStream = new FileInputStream(sourceMessageFileName)) {
            MessageImpl messageImpl = new MessageImpl();
            messageImpl.setContent(InputStream.class, rawInputStream);
            messageImpl.put(Message.CONTENT_TYPE, contentTypeString);

            LOG.debug("Start initializeAttachments");
            new AttachmentDeserializer(messageImpl).initializeAttachments();
            LOG.debug("End initializeAttachments");

            LOG.debug("Start createUserMessage");
            final SOAPMessage soapMessage = soapUtil.createUserMessage(messageImpl);
            LOG.debug("End createUserMessage");

            Messaging messaging = messageUtil.getMessaging(soapMessage);
            LOG.debug("Finished parsing UserMessage");
            return messaging.getUserMessage();

        } catch (Exception e) {
            //TODO notify the backend that an error occured EDELIVERY-4089
            LOG.error("Error parsing the source file [{}]", sourceMessageFileName, e);
            throw new WebServiceException(e);
        }
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

        int maxReadBufferSize = 8 * 1024; //8KB
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

    protected String getFragmentFileName(File outputDirectory, String sourceFileName, long fragmentNumber) {
        return outputDirectory.getAbsolutePath() + File.separator + sourceFileName + FRAGMENT_FILENAME_SEPARATOR + fragmentNumber;
    }

    protected void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if (val != -1) {
            bw.write(buf);
        }
    }
}
