package eu.domibus.ebms3.receiver;

import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.configuration.storage.Storage;
import eu.domibus.core.message.fragment.SplitAndJoinService;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.util.MessageUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.AttachmentInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.http.entity.ContentType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * CXF interceptor responsible for saving on the file system the multi-part mime for the source message
 *
 * @author Cosmin Baciu
 * @since 4.1
 */
public class SaveRequestToFileInInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SaveRequestToFileInInterceptor.class);

    public SaveRequestToFileInInterceptor() {
        super(Phase.RECEIVE);
        this.addBefore(AttachmentInInterceptor.class.getName());
    }

    protected DomibusPropertyProvider domibusPropertyProvider;

    protected DomainContextProvider domainContextProvider;

    protected SplitAndJoinService splitAndJoinService;

    protected MessageUtil messageUtil;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public void handleMessage(Message message) throws Fault {
        Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
        String messageId = getHeaderValue(headers, MSHDispatcher.HEADER_DOMIBUS_MESSAGE_ID);
        boolean compression = Boolean.valueOf(getHeaderValue(headers, MSHDispatcher.HEADER_DOMIBUS_SPLITTING_COMPRESSION));
        String domain = getHeaderValue(headers, MSHDispatcher.HEADER_DOMIBUS_DOMAIN);
        String encoding = (String) message.get(Message.ENCODING);
        String contentType = (String) message.get(Message.CONTENT_TYPE);
        LOG.putMDC(Message.CONTENT_TYPE, contentType);

        final String temporaryDirectoryLocation = domibusPropertyProvider.getProperty(Storage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION);
        if (StringUtils.isEmpty(temporaryDirectoryLocation)) {
            throw new Fault(new DomibusCoreException(DomibusCoreErrorCode.DOM_001, "Could not store Source Message: the property [" + Storage.TEMPORARY_ATTACHMENT_STORAGE_LOCATION + "] is not defined"));
        }

        String fileName = splitAndJoinService.generateSourceFileName(temporaryDirectoryLocation);

        try (FileOutputStream cos = new FileOutputStream(new File(fileName));
             InputStream in = message.getContent(InputStream.class)) {
            LOG.debug("Start copying message [{}] to file [{}]", messageId, fileName);
            IOUtils.copy(in, cos, 32 * 1024);
            in.close();
            cos.close();
            LOG.debug("Finished copying message [{}] to file [{}]", messageId, fileName);

            replaceSoapEnvelope(message, contentType);

            LOG.putMDC(MSHSourceMessageWebservice.SOURCE_MESSAGE_FILE, fileName);
            LOG.putMDC(MSHDispatcher.HEADER_DOMIBUS_SPLITTING_COMPRESSION, String.valueOf(compression));
            LOG.putMDC(MSHDispatcher.HEADER_DOMIBUS_DOMAIN, domain);
        } catch (IOException e) {
            LOG.error("Could not store message into temporary location [{}]", temporaryDirectoryLocation);
            throw new Fault(e);
        }
    }

    protected void replaceSoapEnvelope(Message message, String contentTypeHeader) throws IOException {
        final ContentType contentType = ContentType.parse(contentTypeHeader);
        final String boundary = contentType.getParameter("boundary");
        try (InputStream soapEnvelopeTemplateStream = new ClassPathResource("templates/soapEnvelopeTemplate.xml").getInputStream()) {
            String soapEnvelopeTemplate = IOUtils.toString(soapEnvelopeTemplateStream, "UTF-8");
            LOG.trace("soapEnvelopeTemplate [{}]", soapEnvelopeTemplate);
            String soapContent = StringUtils.replace(soapEnvelopeTemplate, "{boundary}", boundary);
            LOG.trace("New soapContent [{}]", soapContent);

            LOG.debug("Replacing Soap envelope content");
            message.setContent(InputStream.class, IOUtils.toInputStream(soapContent, "UTF-8"));
        }
    }

    protected String getHeaderValue(Map<String, List<String>> headers, String headerName) {
        final List<String> headerValue = headers.get(headerName);
        if (headerValue != null && headerValue.size() > 0) {
            return headerValue.iterator().next();
        }
        return null;

    }

    public void setDomibusPropertyProvider(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    public void setDomainContextProvider(DomainContextProvider domainContextProvider) {
        this.domainContextProvider = domainContextProvider;
    }

    public void setSplitAndJoinService(SplitAndJoinService splitAndJoinService) {
        this.splitAndJoinService = splitAndJoinService;
    }

    public void setMessageUtil(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }
}
