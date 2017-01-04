package eu.domibus.ebms3.receiver;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Component;

@Component(value = "clearMDCInterceptor")
public class ClearMDCInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(ClearMDCInterceptor.class);

    public ClearMDCInterceptor() {
        super(Phase.SETUP_ENDING);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        LOGGER.debug("handleMessage");
        clearMDC();
    }

    @Override
    public void handleFault(Message message) {
        LOGGER.debug("handleFault");
        clearMDC();
    }

    private void clearMDC() {
        LOGGER.info("Clearing MDC messageId");
        LOGGER.removeMDC(DomibusLogger.MDC_MESSAGE_ID);
    }
}
