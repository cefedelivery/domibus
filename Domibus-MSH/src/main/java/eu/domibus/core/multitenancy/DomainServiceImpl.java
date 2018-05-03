package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.multitenancy.dao.DomainDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class DomainServiceImpl implements DomainService {

    private  static final String SGS_CLUSTERED_SCHEDULER = "SgsClusteredScheduler";
    private static final String DOMIBUS_DATABASE_SCHEMA = "domibus.database.schema";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainDao domainDao;

    //TODO add caching
    @Override
    public List<Domain> getDomains() {
        return domainDao.findAll();
    }

    //TODO add caching
    @Override
    public Domain getDomain(String code) {
        final List<Domain> domains = domainDao.findAll();
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

    @Override
    public Domain getDomainForScheduler(String schedulerName) {
        if (SGS_CLUSTERED_SCHEDULER.equalsIgnoreCase(schedulerName)) {
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
            result = "SgsClusteredScheduler";
        }
        return result;
    }

}
