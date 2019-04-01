package eu.domibus.ebms3.receiver;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.metrics.Counter;
import eu.domibus.common.metrics.Timer;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.receiver.handler.IncomingMessageHandler;
import eu.domibus.ebms3.receiver.handler.IncomingMessageHandlerFactory;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.*;
import javax.xml.ws.soap.SOAPBinding;

/**
 * This method is responsible for the receiving of ebMS3 messages and the sending of signal messages like receipts or ebMS3 errors in return
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */

@WebServiceProvider(portName = "mshPort", serviceName = "mshService")
@ServiceMode(Service.Mode.MESSAGE)
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class MSHWebservice implements Provider<SOAPMessage> {


    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MSHWebservice.class);

    private static final String INCOMING_MESSAGES = "incoming_messages";

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    protected IncomingMessageHandlerFactory incomingMessageHandlerFactory;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, timeout = 1200) // 20 minutes
    @Timer(INCOMING_MESSAGES)
    @Counter(INCOMING_MESSAGES)
    public SOAPMessage invoke(final SOAPMessage request) {
        Messaging messaging = messageUtil.getMessage(request);

        LOG.trace("Message received");
        final IncomingMessageHandler messageHandler = incomingMessageHandlerFactory.getMessageHandler(request, messaging);
        if (messageHandler == null) {
            EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Unrecognized message", messaging.getUserMessage().getMessageInfo().getMessageId(), null);
            ex.setMshRole(MSHRole.RECEIVING);
            throw new WebServiceException(ex);
        }

        return messageHandler.processMessage(request, messaging);
    }
}
