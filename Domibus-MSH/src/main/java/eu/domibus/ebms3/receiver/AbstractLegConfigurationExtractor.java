package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
public abstract class AbstractLegConfigurationExtractor implements LegConfigurationExtractor {

    protected final SoapMessage message;

    protected final Messaging messaging;

    protected static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AbstractLegConfigurationExtractor.class);


    AbstractLegConfigurationExtractor(final SoapMessage message, final Messaging messaging) {
        this.message = message;
        this.messaging = messaging;
    }


    protected abstract String getMessageId();


    void setUpMessage(final String pmodeKey) {
        //set the messageId in the MDC context
        LOG.putMDC(DomibusLogger.MDC_MESSAGE_ID, getMessageId());
        message.getExchange().put(MessageInfo.MESSAGE_ID_CONTEXT_PROPERTY, getMessageId());
        message.put(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey);
        //FIXME: Test!!!!
        message.getExchange().put(MSHDispatcher.PMODE_KEY_CONTEXT_PROPERTY, pmodeKey);
    }


}
