package eu.domibus.wss4j.common.crypto;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.spring.SpringContextProvider;
import eu.domibus.wss4j.common.crypto.api.DomainCertificateProvider;
import eu.domibus.wss4j.common.crypto.api.DomainCertificateProviderFactory;
import eu.domibus.wss4j.common.crypto.api.DomainPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class DomainCertificateProviderFactoryImpl implements DomainCertificateProviderFactory {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainCertificateProviderFactoryImpl.class);

    @Autowired
    protected DomainPropertyProvider domainPropertyProvider;

    @Override
    public DomainCertificateProvider createDomainCertificateProvider(String domain) {
        LOG.debug("Creating the certificate provider for domain [{}]", domain);

        return SpringContextProvider.getApplicationContext().getBean(DomainCertificateProviderImpl.class, domain);
    }




}
