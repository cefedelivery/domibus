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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ClearMDCInterceptor.class);

    public ClearMDCInterceptor() {
        super(Phase.SETUP_ENDING);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        LOG.debug("handleMessage");
        clearMDC();
    }

    @Override
    public void handleFault(Message message) {
        LOG.debug("handleFault");
        clearMDC();
    }

    private void clearMDC() {
        LOG.removeMDC(DomibusLogger.MDC_MESSAGE_ID);
        LOG.info("Cleared MDC property [{}]", LOG.translateMDCKey(DomibusLogger.MDC_MESSAGE_ID));
    }
}
