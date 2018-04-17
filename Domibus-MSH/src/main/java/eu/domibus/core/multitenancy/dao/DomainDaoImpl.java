package eu.domibus.core.multitenancy.dao;


import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.crypto.api.DomainPropertyProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    protected DomainPropertyProvider domainPropertyProvider;

    @Override
    public List<Domain> findAll() {
        final String propertyValue = domainPropertyProvider.getPropertyValue(DomibusConfigurationService.DOMIBUS_CONFIG_LOCATION);
        File confDirectory = new File(propertyValue);
        final Collection<File> propertyFiles = FileUtils.listFiles(confDirectory, DOMAIN_FILE_EXTENSION, false);

        List<Domain> result = new ArrayList<>();
        if (propertyFiles == null) {
            return result;
        }
        result.add(DomainService.DEFAULT_DOMAIN);
        for (File propertyFile : propertyFiles) {
            final String fileName = propertyFile.getName();
            if (StringUtils.containsIgnoreCase(fileName, DOMAIN_FILE_SUFFIX)) {
                String domainCode = StringUtils.substringBefore(fileName, DOMAIN_FILE_SUFFIX);
                Domain domain = new Domain(domainCode, domainCode);
                result.add(domain);
            }

        }
        return result;
    }
}

