package eu.domibus.ebms3.receiver;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.configuration.storage.StorageProvider;
import eu.domibus.core.message.fragment.MessageGroupDao;
import eu.domibus.core.message.fragment.MessageGroupEntity;
import eu.domibus.core.message.fragment.MessageHeaderEntity;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessagingProcessingException;
import eu.domibus.plugin.handler.DatabaseMessageHandler;
import eu.domibus.plugin.transformer.impl.UserMessageFactory;
import eu.domibus.util.MessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.message.Message;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@WebServiceProvider(portName = "local-msh-dispatch", serviceName = "local-msh-dispatch-service")
@ServiceMode(Service.Mode.MESSAGE)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class MSHSourceMessageWebservice implements Provider<SOAPMessage> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHSourceMessageWebservice.class);

    public static final String SOURCE_MESSAGE_FILE = "sourceMessageFile";
    private static final Long MB_IN_BYTES = 1048576L;

    @Autowired
    protected MessageFactory messageFactory;

    @Qualifier("jaxbContextEBMS")
    @Autowired
    protected JAXBContext jaxbContext;

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected MessageGroupDao messageGroupDao;

    @Autowired
    StorageProvider storageProvider;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    protected UserMessageFactory userMessageFactory;

    @Autowired
    private DatabaseMessageHandler databaseMessageHandler;

    @Autowired
    private UserMessageLogDao userMessageLogDao;

    @WebMethod
    @WebResult(name = "soapMessageResult")
    public SOAPMessage invoke(final SOAPMessage request) {
        Messaging messaging = null;
        UserMessage userMessage = null;
        try {
            messaging = MessageUtil.getMessaging(request, jaxbContext);
            userMessage = messaging.getUserMessage();
        } catch (SOAPException | JAXBException e) {
            LOG.error("Could not parse Messaging from message", e);
            throw new WebServiceException(e);
        }

        final String domain = LOG.getMDC(MSHDispatcher.HEADER_DOMIBUS_DOMAIN);
        domainContextProvider.setCurrentDomain(domain);

        final String contentTypeString = LOG.getMDC(Message.CONTENT_TYPE);
        final boolean compression = Boolean.valueOf(LOG.getMDC(MSHDispatcher.HEADER_DOMIBUS_SPLITTING_COMPRESSION));
        final String sourceMessageFileName = LOG.getMDC(MSHSourceMessageWebservice.SOURCE_MESSAGE_FILE);
        final ContentType contentType = ContentType.parse(contentTypeString);

        MessageGroupEntity messageGroupEntity = new MessageGroupEntity();
        messageGroupEntity.setGroupId(UUID.randomUUID().toString());
        final File sourceMessageFile = new File(sourceMessageFileName);
        if (compression) {
            messageGroupEntity.setCompressedMessageSize(sourceMessageFile.length());
            messageGroupEntity.setCompressionAlgorithm("application/gzip");
        } else {
            messageGroupEntity.setMessageSize(sourceMessageFile.length());
        }

        messageGroupEntity.setSoapAction("");
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

        final int fragmentSize = legConfiguration.getSplitting().getFragmentSize();
        long fragmentCount = getFragmentCount(sourceMessageFile, fragmentSize);

        messageGroupEntity.setFragmentCount(fragmentCount);
        List<String> fragmentFiles = null;
        try {
            fragmentFiles = splitSourceMessage(sourceMessageFile, fragmentCount);
        } catch (IOException e) {
            LOG.error("Could not split source message", e);
            throw new WebServiceException(e);
        }
        LOG.debug("Deleting source file [{}]", sourceMessageFile);
        sourceMessageFile.delete();
        LOG.debug("Finished deleting source file [{}]", sourceMessageFile);

        MessageHeaderEntity messageHeaderEntity = new MessageHeaderEntity();
        messageHeaderEntity.setBoundary(contentType.getParameter("boundary"));
        final String start = contentType.getParameter("start");
        messageHeaderEntity.setStart(StringUtils.replaceEach(start, new String[]{"<", ">"}, new String[]{"",""}));
        messageGroupEntity.setMessageHeaderEntity(messageHeaderEntity);
        messageGroupDao.create(messageGroupEntity);

        String backendName = userMessageLogDao.findBackendForMessageId(userMessage.getMessageInfo().getMessageId());
        for (int index = 0; index < fragmentFiles.size(); index++) {
            try {
                final String fragmentFile = fragmentFiles.get(index);
                createMessagingForFragment(userMessage, messageGroupEntity, backendName, fragmentFile, index + 1);
                LOG.debug("Deleting fragment file [{}]", fragmentFile);
                new File(fragmentFile).delete();
            } catch (MessagingProcessingException e) {
                LOG.error("Could not create messagin for fragment [{}]", index);
                throw new WebServiceException(e);
            }
        }

        try {
            SOAPFactory soapFac = SOAPFactory.newInstance();
            SOAPMessage responseMessage = messageFactory.createMessage();
            QName sayHi = new QName("http://apache.org/hello_world_rpclit", "sayHiWAttach");
            responseMessage.getSOAPBody().addChildElement(soapFac.createElement(sayHi));
            responseMessage.saveChanges();

            LOG.info("Invoke [{}]", request);
            return responseMessage;
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    protected void createMessagingForFragment(UserMessage userMessage, MessageGroupEntity messageGroupEntity, String backendName, String fragmentFile, int index) throws MessagingProcessingException {
        final UserMessage userMessageFragment = userMessageFactory.createUserMessageFragment(userMessage, messageGroupEntity, index, fragmentFile);
        databaseMessageHandler.submitMessageFragment(userMessageFragment, backendName);
    }


    protected long getFragmentCount(File sourceMessageFile, int fragmentSizeInMB) {
        final long sourceSize = sourceMessageFile.length();
        long fragmentSizeInBytes = fragmentSizeInMB * MB_IN_BYTES;
        long numberOfFragments = sourceSize / fragmentSizeInBytes;
        long remainingFragment = sourceSize % fragmentSizeInBytes;
        long totalNumberOfFragments = numberOfFragments;
        if (remainingFragment > 0) {
            totalNumberOfFragments = numberOfFragments + 1;
        }
        return totalNumberOfFragments;
    }

    protected List<String> splitSourceMessage(File sourceMessageFile, long fragmentCount) throws IOException {
        List<String> result = new ArrayList<>();

        int maxReadBufferSize = 8 * 1024; //8KB
        final long sourceSize = sourceMessageFile.length();


        LOG.debug("File [{}] will be split into [{}] fragments ", sourceMessageFile, fragmentCount);

        long bytesPerSplit = sourceSize / fragmentCount;
        long remainingBytes = sourceSize % fragmentCount;

        Domain currentDomain = domainContextProvider.getCurrentDomainSafely();
        Storage currentStorage = storageProvider.forDomain(currentDomain);
        LOG.debug("Retrieved Storage for domain [{}]", currentDomain);
        if (currentStorage == null) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not retrieve Storage for domain" + currentDomain + " is null");
        }
        if (currentStorage.getStorageDirectory() == null || currentStorage.getStorageDirectory().getName() == null) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not store fragment payload. Please configure " + Storage.ATTACHMENT_STORAGE_LOCATION + " when using SplitAndJoin");
        }

        try (RandomAccessFile raf = new RandomAccessFile(sourceMessageFile, "r")) {
            for (int index = 1; index <= fragmentCount; index++) {
                final String fragmentFileName = getFragmentFileName(currentStorage.getStorageDirectory(), sourceMessageFile.getName(), index);
                result.add(fragmentFileName);
                saveFragmentPayload(bytesPerSplit, maxReadBufferSize, raf, fragmentFileName);
            }
            if (remainingBytes > 0) {
                final String remainingFragmentFileName = getFragmentFileName(currentStorage.getStorageDirectory(), sourceMessageFile.getName(), (fragmentCount + 1));
                result.add(remainingFragmentFileName);
                try (BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(remainingFragmentFileName))) {
                    readWrite(raf, bw, remainingBytes);
                }
            }
        }
        return result;
    }

    protected void saveFragmentPayload(long bytesPerSplit, int maxReadBufferSize, RandomAccessFile raf, final String fragmentFileName) throws IOException {
        try (BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(fragmentFileName))) {
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
        return outputDirectory.getAbsolutePath() + File.separator  + sourceFileName + "_" + fragmentNumber;
    }

    protected void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if (val != -1) {
            bw.write(buf);
        }
    }
}
