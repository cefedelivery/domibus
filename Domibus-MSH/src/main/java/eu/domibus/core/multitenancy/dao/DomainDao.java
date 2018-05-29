package eu.domibus.core.multitenancy.dao;

import eu.domibus.api.multitenancy.Domain;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainDao {

    List<Domain> findAll();


}
