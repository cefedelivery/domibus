package eu.domibus.ebms3.receiver;

import eu.domibus.wss4j.common.crypto.BlockUtil;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomibusReadyInterceptor extends AbstractPhaseInterceptor<Message> {

    private final static Logger LOG = LoggerFactory.getLogger(DomibusReadyInterceptor.class);

    public DomibusReadyInterceptor() {
        super("receive");
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        if(!BlockUtil.getInitiated()){
            throw new Fault(new IllegalStateException("Server starting"));
        }
    }

}
