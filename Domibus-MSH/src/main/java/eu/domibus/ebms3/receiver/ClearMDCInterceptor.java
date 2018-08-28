package eu.domibus.ebms3.receiver;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "clearMDCInterceptor")
public class ClearMDCInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ClearMDCInterceptor.class);

    @Autowired
    protected DomainContextProvider domainContextProvider;

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
        LOG.debug("Clearing message ID MDC property [{}]", LOG.getMDCKey(DomibusLogger.MDC_MESSAGE_ID));
        LOG.removeMDC(DomibusLogger.MDC_MESSAGE_ID);

        LOG.debug("Clearing domain [{}]", domainContextProvider.getCurrentDomainSafely());
        domainContextProvider.clearCurrentDomain();
    }
}
