package eu.domibus.clustering;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.messaging.MessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Collection;


/**
 * Created by kochc01 on 02.03.2016.
 */

@Service(value = "controllerListenerService")
public class ControllerListenerService implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ControllerListenerService.class);

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    protected MultiDomainCryptoService multiDomainCryptoService;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Override
    @Transactional
    public void onMessage(Message message) {
        LOG.info("On message: ", message.toString());
        String command = null;
        try {
            command = message.getStringProperty(Command.COMMAND);
        } catch (JMSException e) {
            LOG.error("Could not parse command", e);
            return;
        }
        if (command == null) {
            LOG.error("Received null command");
            return;
        }

        Domain domain = null;
        try {
            String domainCode = message.getStringProperty(MessageConstants.DOMAIN);
            domain = domainService.getDomain(domainCode);
            domainContextProvider.setCurrentDomain(domainCode);
        } catch (JMSException e) {
            LOG.error("Could not get the domain", e);
            return;
        }

        switch (command) {
            case Command.RELOAD_PMODE:
                pModeProvider.refresh();
                multiDomainCryptoService.refreshTrustStore(domain);
                break;
            case Command.EVICT_CACHES:
                Collection<String> cacheNames = cacheManager.getCacheNames();
                for (String cacheName : cacheNames) {
                    cacheManager.getCache(cacheName).clear();
                }
                break;
            case Command.RELOAD_TRUSTSTORE:
                LOG.info("refreshTrustStore domain [{}]", domain);
                multiDomainCryptoService.refreshTrustStore(domain);
                break;
            default:
                LOG.error("Unknown command received: " + command);
        }
    }
}
