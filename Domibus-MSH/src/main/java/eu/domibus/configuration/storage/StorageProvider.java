package eu.domibus.configuration.storage;

import eu.domibus.api.multitenancy.Domain;

/**
 * @author Ion Perpegel
 * @since 4.0
 */

public interface StorageProvider {

    Storage forDomain(Domain domain) ;

    Storage getCurrentStorage();

    boolean savePayloadsInDatabase();

}
