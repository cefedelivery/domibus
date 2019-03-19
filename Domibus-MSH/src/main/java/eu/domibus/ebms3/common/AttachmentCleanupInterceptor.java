package eu.domibus.ebms3.common;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.attachment.AttachmentDataSource;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;

import javax.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * CXF is storing in tmp files the payloads higher then 200Mb.
 * This interceptor has the responsibility to close all the input streams of the attachments' data sources.
 * When an input stream is closed the underlying resource is released and can be garbage collected.
 *
 * @author Federico Martini
 * @since 3.2.2
 */
public class AttachmentCleanupInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AttachmentCleanupInterceptor.class);

    public AttachmentCleanupInterceptor() {
        super(Phase.PREPARE_SEND_ENDING);
    }

    @Autowired
    protected  AttachmentCleanupService attachmentCleanupService;

    public void handleMessage(Message message) throws Fault {
        Exchange exchange = message.getExchange();
        cleanRequestAttachment(exchange);
    }

    public void handleFault(Message message) {
        Exchange exchange = message.getExchange();
        cleanRequestAttachment(exchange);
    }

    private void cleanRequestAttachment(Exchange exchange) {
        if (exchange.getOutMessage() != null) {
            LOG.debug("Closing outbound message attachments' input streams");
            attachmentCleanupService.cleanAttachments(exchange.getOutMessage().getAttachments());
        }
        if (exchange.getInMessage() != null) {
            LOG.debug("Closing inbound message attachments' input streams");

            //the attachment collection is not yet initialized when sending large files and the request is not XSD valid
            try {
                attachmentCleanupService.cleanAttachments(exchange.getInMessage().getAttachments());
            } catch (Exception e) {
                LOG.warn("Could not clean inbound message attachments", e);
            }

        }
    }


}
