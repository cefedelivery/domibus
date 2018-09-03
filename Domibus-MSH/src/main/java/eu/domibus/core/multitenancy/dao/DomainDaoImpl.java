package eu.domibus.core.multitenancy.dao;


import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.services.DomibusCacheService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Component
public class DomainDaoImpl implements DomainDao {

    private static final String[] DOMAIN_FILE_EXTENSION = {"properties"};
    private static final String DOMAIN_FILE_SUFFIX = "-domibus";
    private static final String DOMAIN_TITLE = "domain.title";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Cacheable(value = DomibusCacheService.ALL_DOMAINS_CACHE)
    @Override
    public List<Domain> findAll() {
        List<Domain> result = new ArrayList<>();
        result.add(DomainService.DEFAULT_DOMAIN);
        if (!domibusConfigurationService.isMultiTenantAware()) {
            return result;
        }

        final String propertyValue = domibusConfigurationService.getConfigLocation();
        File confDirectory = new File(propertyValue);
        final Collection<File> propertyFiles = FileUtils.listFiles(confDirectory, DOMAIN_FILE_EXTENSION, false);


        if (propertyFiles == null) {
            return result;
        }
        for (File propertyFile : propertyFiles) {
            final String fileName = propertyFile.getName();
            if (StringUtils.containsIgnoreCase(fileName, DOMAIN_FILE_SUFFIX)) {
                String domainCode = StringUtils.substringBefore(fileName, DOMAIN_FILE_SUFFIX);

                Domain domain = new Domain(domainCode, null);
                domain.setName(getDomainTitle(domain));
                result.add(domain);
            }

        }
        return result;
    }

    protected String getDomainTitle(Domain domain) {
        return domibusPropertyProvider.getProperty(domain, DOMAIN_TITLE, domain.getCode());
    }
}

