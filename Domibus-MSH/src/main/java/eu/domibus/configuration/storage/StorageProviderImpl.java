package eu.domibus.configuration.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
@Service
public class StorageProviderImpl implements StorageProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(StorageProviderImpl.class);

    @Autowired
    protected StorageFactory storageFactory;

    @Autowired
    protected DomainService domainService;

    protected Map<Domain, Storage> instances = new HashMap<>();

    @PostConstruct
    public void init() {
        final List<Domain> domains = domainService.getDomains();
        for (Domain domain : domains) {
            Storage instance = storageFactory.create(domain);
            instances.put(domain, instance);
            LOG.info("Storage initialized for domain [{}]", domain);
        }
    }

    @Override
    public Storage forDomain(Domain domain) {
        return instances.get(domain);
    }

}
