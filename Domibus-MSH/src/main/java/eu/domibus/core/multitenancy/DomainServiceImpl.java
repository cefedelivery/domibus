package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.core.multitenancy.dao.DomainDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainServiceImpl implements DomainService {

    private  static final String DEFAULT_QUARTZ_SCHEDULER_NAME = "schedulerFactoryBean";
    private static final String DOMIBUS_DATABASE_SCHEMA = "domibus.database.schema";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainDao domainDao;


    @Override
    public List<Domain> getDomains() {
        return domainDao.findAll();
    }

    @Cacheable(value = DomibusCacheService.DOMAIN_BY_CODE_CACHE, key = "#code")
    @Override
    public Domain getDomain(String code) {
        final List<Domain> domains = getDomains();
        if (domains == null) {
            return null;
        }
        for (Domain domain : domains) {
            if (StringUtils.equalsIgnoreCase(code, domain.getCode())) {
                return domain;
            }
        }
        return null;
    }

    @Cacheable(value = DomibusCacheService.DOMAIN_BY_SCHEDULER_CACHE, key = "#schedulerName")
    @Override
    public Domain getDomainForScheduler(String schedulerName) {
        if (DEFAULT_QUARTZ_SCHEDULER_NAME.equalsIgnoreCase(schedulerName)) {
            return DomainService.DEFAULT_DOMAIN;
        }
        return getDomain(schedulerName);
    }

    @Override
    public String getDatabaseSchema(Domain domain) {
        return domibusPropertyProvider.getProperty(domain, DOMIBUS_DATABASE_SCHEMA);
    }

    @Override
    public String getGeneralSchema() {
        return domibusPropertyProvider.getProperty(DomainService.GENERAL_SCHEMA_PROPERTY);
    }

    @Override
    public String getSchedulerName(Domain domain) {
        String result = domain.getCode();
        if (DomainService.DEFAULT_DOMAIN.equals(domain)) {
            //keep the same name used in Domibus 3.3.x in order not to break the backward compatibility; if scheduler name is changed, a DB migration script is needed
            result = DEFAULT_QUARTZ_SCHEDULER_NAME;
        }
        return result;
    }

}
