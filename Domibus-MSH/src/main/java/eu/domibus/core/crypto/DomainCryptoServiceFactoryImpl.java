package eu.domibus.core.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.crypto.api.DomainCryptoService;
import eu.domibus.core.crypto.api.DomainCryptoServiceFactory;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainCryptoServiceFactoryImpl implements DomainCryptoServiceFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCryptoServiceFactoryImpl.class);

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public DomainCryptoService createDomainCryptoService(Domain domain) {
        LOG.debug("Creating the certificate provider for domain [{}]", domain);

        return applicationContext.getBean(DomainCryptoServiceImpl.class, domain);
    }


}
