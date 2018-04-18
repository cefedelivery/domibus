package eu.domibus.api.multitenancy;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainService {

    Domain DEFAULT_DOMAIN = new Domain("default", "Default");

    String GENERAL_SCHEMA_PROPERTY = "domibus.database.general.schema";

    List<Domain> getDomains();

    Domain getDomain(String code);

    Domain getDomainForScheduler(String schedulerName);

    String getDatabaseSchema(Domain domain);

    String getGeneralSchema();

    String getSchedulerName(Domain domain);

}
