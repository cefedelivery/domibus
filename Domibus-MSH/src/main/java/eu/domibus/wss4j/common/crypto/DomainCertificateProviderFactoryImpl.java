package eu.domibus.wss4j.common.crypto;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.spring.SpringContextProvider;
import eu.domibus.wss4j.common.crypto.api.DomainCertificateProvider;
import eu.domibus.wss4j.common.crypto.api.DomainCertificateProviderFactory;
import eu.domibus.wss4j.common.crypto.api.DomainPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainCertificateProviderFactoryImpl implements DomainCertificateProviderFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCertificateProviderFactoryImpl.class);

    @Autowired
    protected DomainPropertyProvider domainPropertyProvider;

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public DomainCertificateProvider createDomainCertificateProvider(Domain domain) {
        LOG.debug("Creating the certificate provider for domain [{}]", domain);

        return applicationContext.getBean(DomainCertificateProviderImpl.class, domain);
    }




}
