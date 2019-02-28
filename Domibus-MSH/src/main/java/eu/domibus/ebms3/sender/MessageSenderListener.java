package eu.domibus.ebms3.sender;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Message;


/**
 * This class is responsible for the handling of outgoing messages.
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 * @since 3.0
 */
@Service(value = "messageSenderListener")
public class MessageSenderListener extends AbstractMessageSenderListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessageSenderListener.class);

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 1200) // 20 minutes
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void onMessage(final Message message) {
        LOG.debug("Processing message [{}]", message);
        super.onMessage(message);
    }

}
