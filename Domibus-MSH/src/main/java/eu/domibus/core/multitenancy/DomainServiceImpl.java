package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.crypto.api.DomainPropertyProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainServiceImpl implements DomainService {

    protected Map<String, Domain> domains = new HashMap<>();

    @Autowired
    protected DomainPropertyProvider domainPropertyProvider;

    @PostConstruct
    public void init() {
        domains.put(DomainService.DEFAULT_DOMAIN.getCode(), DomainService.DEFAULT_DOMAIN);
//        domains.put("taxud", new Domain("taxud", "Taxud") );
    }

    //TODO add caching
    @Override
    public List<Domain> getDomains() {
        //TODO get the domains from the database/properties
        return new ArrayList<>(domains.values());
}

    //TODO add caching
    @Override
    public Domain getDomain(String code) {
        //TODO get the domains from the database/properties
        return domains.get(code);
    }

    @Override
    public String getDatabaseSchema(Domain domain) {
        return domainPropertyProvider.getPropertyValue(domain, "domibus.database.schema");
    }

    @Override
    public String getGeneralSchema() {
        return domainPropertyProvider.getPropertyValue(DomainService.GENERAL_SCHEMA_PROPERTY);
    }

    @Override
    public String getSchedulerName(Domain domain) {
        String result = domain.getCode();
        if (DomainService.DEFAULT_DOMAIN.equals(domain)) {
            //keep the same name used in Domibus 3.3.x in order not to break the backward compatibility; if scheduler name is changed, a DB migration script is needed
            result = "SgsClusteredScheduler";
        }
        return result;
    }

    @Override
    public Domain getDomainForScheduler(String schedulerName) {
        if("SgsClusteredScheduler".equalsIgnoreCase(schedulerName)) {
            return DomainService.DEFAULT_DOMAIN;
        }
        return getDomain(schedulerName);
    }
}
