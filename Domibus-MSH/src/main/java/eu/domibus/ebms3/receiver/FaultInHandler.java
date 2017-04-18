package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.ebms3.common.handler.AbstractFaultHandler;
import eu.domibus.api.message.ebms3.model.Messaging;
import eu.domibus.ebms3.pmode.exception.NoMatchingPModeFoundException;
import eu.domibus.ebms3.sender.EbMS3MessageBuilder;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.ws.policy.PolicyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.Set;

/**
 * This handler is resposible for creation of ebMS3 conformant error messages
 *
 * @author Christian Koch, Stefan Mueller
 */
public class FaultInHandler extends AbstractFaultHandler {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FaultInHandler.class);

    @Autowired
    private EbMS3MessageBuilder messageBuilder;

    @Autowired
    private ErrorLogDao errorLogDao;


    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public boolean handleMessage(final SOAPMessageContext context) {
        //Do nothing as this is a fault handler
        return true;
    }

    @Override
    /**
     * The {@code handleFault} method is responsible for handling and conversion of exceptions
     * thrown during the processing of incoming ebMS3 messages
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean handleFault(final SOAPMessageContext context) {

        final Exception exception = (Exception) context.get(Exception.class.getName());
        final Throwable cause = exception.getCause();
        EbMS3Exception ebMS3Exception = null;
        if (cause != null) {

            if (!(cause instanceof EbMS3Exception)) {
                //do Mapping of non ebms exceptions
                if (cause instanceof NoMatchingPModeFoundException) {
                    ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, cause.getMessage(), ((NoMatchingPModeFoundException) cause).getMessageId(), cause);
                    ebMS3Exception.setMshRole(MSHRole.RECEIVING);
                } else {

                    if (cause instanceof WebServiceException) {
                        if (cause.getCause() instanceof EbMS3Exception) {
                            ebMS3Exception = (EbMS3Exception) cause.getCause();
                        }
                    } else {
                        //FIXME: use a consistent way of property exchange between JAXWS and CXF message model. This: PhaseInterceptorChain
                        final String messageId = (String) PhaseInterceptorChain.getCurrentMessage().getContextualProperty("ebms.messageid");
                        ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "unknown error occurred", messageId, cause);
                        ebMS3Exception.setMshRole(MSHRole.RECEIVING);
                    }
                }

            } else {
                ebMS3Exception = (EbMS3Exception) cause;
            }

            this.processEbMSError(context, ebMS3Exception);

        } else {
            if (exception instanceof PolicyException) {
                //FIXME: use a consistent way of property exchange between JAXWS and CXF message model. This: PhaseInterceptorChain
                final String messageId = (String) PhaseInterceptorChain.getCurrentMessage().getContextualProperty("ebms.messageid");

                ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0103, exception.getMessage(), messageId, exception);
                ebMS3Exception.setMshRole(MSHRole.RECEIVING);
            } else {
                ebMS3Exception = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0004, "unknown error occurred", null, null);
                ebMS3Exception.setMshRole(MSHRole.RECEIVING);
            }

            this.processEbMSError(context, ebMS3Exception);
        }


        return true;
    }

    private void processEbMSError(final SOAPMessageContext context, final EbMS3Exception ebMS3Exception) {

        // at this point an EbMS3Exception is available in any case
        SOAPMessage soapMessageWithEbMS3Error = null;
        try {
            soapMessageWithEbMS3Error = this.messageBuilder.buildSOAPFaultMessage(ebMS3Exception.getFaultInfo());
        } catch (final EbMS3Exception e) {
            this.errorLogDao.create(new ErrorLogEntry(e));
        }
        context.setMessage(soapMessageWithEbMS3Error);

        final Messaging messaging = this.extractMessaging(soapMessageWithEbMS3Error);

        LOG.businessError(DomibusMessageCode.BUS_MESSAGE_RECEIVE_FAILED, ebMS3Exception, messaging.getSignalMessage().getMessageInfo().getMessageId());

        this.errorLogDao.create(ErrorLogEntry.parse(messaging, MSHRole.RECEIVING));
    }

    @Override
    public void close(final MessageContext context) {

    }
}
