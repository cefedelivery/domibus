package eu.domibus.core.pmode;


import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class PModeProviderFactoryImpl {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeProviderFactoryImpl.class);

    @Autowired
    protected ApplicationContext applicationContext;

    public PModeProvider createDomainPModeProvider(Domain domain) {
        LOG.debug("Creating the PMode provider for domain [{}]", domain);

        return applicationContext.getBean(CachingPModeProvider.class, domain);
    }
}
