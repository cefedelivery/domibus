package eu.domibus.ext.services;

import eu.domibus.ext.domain.DomainDTO;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomibusPropertyExtService {

    String getProperty(String propertyName);

    String getProperty(String propertyName, String defaultValue);

    String getDomainProperty(DomainDTO domain, String propertyName);

    String getDomainProperty(DomainDTO domain, String propertyName, String defaultValue);

    String getDomainResolvedProperty(DomainDTO domain, String propertyName);

    String getResolvedProperty(String propertyName);

}
