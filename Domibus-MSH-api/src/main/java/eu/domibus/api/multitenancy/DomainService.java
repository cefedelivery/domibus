package eu.domibus.api.multitenancy;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainService {

    Domain DEFAULT_DOMAIN = new Domain("default", "Default");

    List<Domain> getDomains();

    Domain getDomain(String code);

    String getDatabaseSchema(Domain domain);
}
