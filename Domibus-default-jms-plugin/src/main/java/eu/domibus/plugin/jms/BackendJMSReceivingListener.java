package eu.domibus.plugin.jms;

import eu.domibus.ext.services.DomainContextService;
import eu.domibus.ext.services.DomibusPropertyService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.MapMessage;

/**
 * @author Cosmin Baciu
 */
@Service
public class BackendJMSReceivingListener  {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendJMSReceivingListener.class);

    @Autowired
    protected BackendJMSImpl backendJMS;

    @Autowired
    protected DomibusPropertyService domibusPropertyService;

    @Autowired
    protected DomainContextService domainContextService;

    /**
     * This method is called when a message was received at the incoming queue
     *
     * @param map The incoming JMS Message
     */
    @JmsListener(destination = "${jmsplugin.queue.in}", containerFactory = "backendJmsListenerContainerFactory")
    //Propagation.REQUIRES_NEW is needed in order to avoid sending the JMS message before the database data is commited; probably this is a bug in Atomikos which will be solved by performing an upgrade
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void receiveMessage(final MapMessage map) {
        backendJMS.receiveMessage(map);
    }

}
