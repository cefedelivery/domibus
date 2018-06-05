package eu.domibus.plugin.jms;

import eu.domibus.ext.services.AuthenticationExtService;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
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
public class BackendJMSReceivingListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendJMSReceivingListener.class);

    @Autowired
    protected BackendJMSImpl backendJMS;

    @Autowired
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Autowired
    protected DomainContextExtService domainContextExtService;

    @Autowired
    protected AuthenticationExtService authenticationExtService;

    @Autowired
    private DomibusConfigurationExtService domibusConfigurationExtService;


    /**
     * This method is called when a message was received at the incoming queue
     *
     * @param map The incoming JMS Message
     */
    @JmsListener(destination = "${jmsplugin.queue.in}", containerFactory = "backendJmsListenerContainerFactory")
    //Propagation.REQUIRES_NEW is needed in order to avoid sending the JMS message before the database data is commited; probably this is a bug in Atomikos which will be solved by performing an upgrade
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void receiveMessage(final MapMessage map) {
        if (domibusConfigurationExtService.isMultiTenantAware()) {
            authenticate(map);
        }

        backendJMS.receiveMessage(map);
    }

    protected void authenticate(final MapMessage map) {
        String username = null;
        String password = null;
        try {
            username = map.getStringProperty(JMSMessageConstants.USERNAME);
            password = map.getStringProperty(JMSMessageConstants.PASSWORD);
        } catch (Exception e) {
            LOG.error("Exception occurred while retrieving the username or password", e);
            throw new DefaultJmsPluginException("Exception occurred while retrieving the username or password", e);
        }
        authenticationExtService.basicAuthenticate(username, password);
    }

}
