package eu.domibus.clustering;

import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.wss4j.common.crypto.api.MultiDomainCertificateProvider;
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
    protected MultiDomainCertificateProvider multiDomainCertificateProvider;

    @Override
    @Transactional
    public void onMessage(Message message) {
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

        String domain = null;
        try {
            domain = message.getStringProperty("domain");
        } catch (JMSException e) {
            LOG.error("Could not get the domain", e);
            return;
        }

        switch (command) {
            case Command.RELOAD_PMODE:
                pModeProvider.refresh();
                multiDomainCertificateProvider.refreshTrustStore(domain);
                break;
            case Command.EVICT_CACHES:
                Collection<String> cacheNames = cacheManager.getCacheNames();
                for (String cacheName : cacheNames) {
                    cacheManager.getCache(cacheName).clear();
                }
                break;
            case Command.RELOAD_TRUSTSTORE:
                multiDomainCertificateProvider.refreshTrustStore(domain);
                break;
            default:
                LOG.error("Unknown command received: " + command);
        }
    }
}
