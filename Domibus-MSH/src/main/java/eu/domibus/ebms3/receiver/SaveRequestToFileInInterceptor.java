package eu.domibus.ebms3.receiver;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.services.SoapService;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.AttachmentInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


@Service
public class SaveRequestToFileInInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SaveRequestToFileInInterceptor.class);

    public SaveRequestToFileInInterceptor() {
        super(Phase.RECEIVE);
        this.addBefore(AttachmentInInterceptor.class.getName());
    }

    @Autowired
    protected SoapService soapService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Override
    public void handleMessage(Message message) throws Fault {
        String encoding = (String) message.get(Message.ENCODING);
        String contentType = (String) message.get(Message.CONTENT_TYPE);
        LOG.putMDC(Message.CONTENT_TYPE, contentType);

        final String temporaryDirectoryLocation = domibusPropertyProvider.getProperty(Storage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION);
        if (StringUtils.isEmpty(temporaryDirectoryLocation)) {
            throw new Fault(new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not store Source Message: the property [" + Storage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION + "] is not defined"));
        }

        InputStream in = message.getContent(InputStream.class);
        CachedOutputStream cos = new CachedOutputStream();
        try {
            cacheMessageContent(message, temporaryDirectoryLocation, in, cos);

            String fileName = generateSourceFileName(temporaryDirectoryLocation);
            copyMessageContentToFile(cos, fileName);
            LOG.putMDC(MSHSourceMessageWebservice.SOURCE_MESSAGE_FILE, fileName);
        } finally {
            try {
                cos.close();
            } catch (IOException e) {
                LOG.error("Could not close output stream", e);
            }
        }
    }

    protected void copyMessageContentToFile(CachedOutputStream cos, String fileName) {
        try {
            FileUtils.copyInputStreamToFile(cos.getInputStream(), new File(fileName));
        } catch (IOException e) {
            LOG.error("Could not copy the message content to file " + fileName);
            throw new Fault(e);
        }
    }

    protected String generateSourceFileName(String temporaryDirectoryLocation) {
        final String uuid = UUID.randomUUID().toString();
        return temporaryDirectoryLocation + "/" + uuid;
    }


    protected void cacheMessageContent(Message message, String temporaryDirectoryLocation, InputStream in, CachedOutputStream cos) {
        try {
            cos.setOutputDir(new File(temporaryDirectoryLocation));
            IOUtils.copy(in, cos);
            message.setContent(InputStream.class, cos.getInputStream());
        } catch (IOException e) {
            LOG.error("Could not store message into temporary location [{}]", temporaryDirectoryLocation);
            throw new Fault(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error("Could not close input stream", e);
                }
            }
        }
    }
}
