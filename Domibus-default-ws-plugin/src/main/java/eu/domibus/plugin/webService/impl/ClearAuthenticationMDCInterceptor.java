package eu.domibus.plugin.webService.impl;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.stereotype.Component;

@Component(value = "clearAuthenticationMDCInterceptor")
public class ClearAuthenticationMDCInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(ClearAuthenticationMDCInterceptor.class);

    public ClearAuthenticationMDCInterceptor() {
        super(Phase.SETUP_ENDING);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        LOGGER.debug("handleMessage");
        clearAuthenticationMDC();
    }

    @Override
    public void handleFault(Message message) {
        LOGGER.debug("handleFault");
        clearAuthenticationMDC();
    }

    private void clearAuthenticationMDC() {
        LOGGER.info("Clearing MDC user");
        LOGGER.removeMDC(DomibusLogger.MDC_USER);
    }
}
