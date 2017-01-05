package eu.domibus.ebms3.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.attachment.AttachmentDataSource;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

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

    private static final Log LOG = LogFactory.getLog(AttachmentCleanupInterceptor.class);

    public AttachmentCleanupInterceptor() {
        super(Phase.PREPARE_SEND_ENDING);
    }

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
            cleanAttachments(exchange.getOutMessage().getAttachments());
        }
        if (exchange.getInMessage() != null) {
            LOG.debug("Closing inbound message attachments' input streams");
            cleanAttachments(exchange.getInMessage().getAttachments());
        }
    }

    private void cleanAttachments(Collection<Attachment> attachments) {
        if (attachments != null) {
            for (Attachment attachment : attachments) {
                try {
                    cleanRequestAttachment(attachment);
                } catch (IOException e) {
                    LOG.error("Could not close the input stream of this attachment [" + attachment.getId() + "]", e);
                }
            }
        }
    }

    private void cleanRequestAttachment(Attachment attachment) throws IOException {

        if (attachment == null || attachment.getDataHandler() == null || attachment.getDataHandler().getDataSource() == null)
            return;

        DataSource ds = attachment.getDataHandler().getDataSource();
        if (ds instanceof AttachmentDataSource) {
            InputStream is = ds.getInputStream();
            // close will delete resource(etc: temporary file) held by the input stream;
            is.close();
            LOG.debug("Input stream successfully closed");
        } else {
            LOG.debug("Data source is [" + ds.getClass().getName() + "] and for content type [" + ds.getContentType() + "]");
        }
    }
}
