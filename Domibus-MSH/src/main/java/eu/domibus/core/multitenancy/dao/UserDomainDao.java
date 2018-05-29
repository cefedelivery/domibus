package eu.domibus.core.multitenancy.dao;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface UserDomainDao {

    String findDomainByUser(String userName);
}
