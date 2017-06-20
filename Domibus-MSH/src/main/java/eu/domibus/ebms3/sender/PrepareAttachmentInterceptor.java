package eu.domibus.ebms3.sender;

import eu.domibus.util.SoapUtil;
import org.apache.cxf.attachment.AttachmentImpl;
import org.apache.cxf.attachment.AttachmentUtil;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Attachment;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.wsdl.interceptors.BareOutInterceptor;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public class PrepareAttachmentInterceptor extends AbstractPhaseInterceptor<Message> {
    public PrepareAttachmentInterceptor() {
        super(Phase.MARSHAL);
        super.addBefore(BareOutInterceptor.class.getName());
    }

    /**
     * Intercepts a message.
     * Interceptors should NOT invoke handleMessage or handleFault
     * on the next interceptor - the interceptor chain will
     * take care of this.
     *
     * @param message message to handle
     */
    @Override
    public void handleMessage(final Message message) throws Fault {
        final SOAPMessage soapMessage = message.getContent(SOAPMessage.class);
        if (soapMessage.countAttachments() > 0) {
            if (message.getAttachments() == null) {
                message.setAttachments(new ArrayList<Attachment>(soapMessage
                        .countAttachments()));
            }
            final Iterator<AttachmentPart> it = CastUtils.cast(soapMessage.getAttachments());
            while (it.hasNext()) {
                final AttachmentPart part = it.next();
                final String id = AttachmentUtil.cleanContentId(part.getContentId());
                final AttachmentImpl att = new AttachmentImpl(id);
                try {
                    att.setDataHandler(part.getDataHandler());
                } catch (final SOAPException e) {
                    throw new Fault(e);
                }
                final Iterator<MimeHeader> it2 = CastUtils.cast(part.getAllMimeHeaders());
                while (it2.hasNext()) {
                    final MimeHeader header = it2.next();
                    att.setHeader(header.getName(), header.getValue());
                }
                message.getAttachments().add(att);
            }
        }
        message.getInterceptorChain().add(new SetPolicyOutInterceptor.LogAfterPolicyCheckInterceptor());
    }

}

