package eu.domibus.plugin.fs.queue;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.MDCKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Message;
import javax.jms.MessageListener;

@Service("fsOutMessageListener")
public class FSOutMessageListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSOutMessageListener.class);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    @Override
    public void onMessage(Message message) {
        LOG.debug("onMessage start");
    }
}
