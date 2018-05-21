package eu.domibus.core.multitenancy.dao;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface UserDomainDao {

    String findDomainByUser(String userName);

    String findPreferredDomainByUser(String userName);

    List<UserDomainEntity> listPreferredDomains();
}
