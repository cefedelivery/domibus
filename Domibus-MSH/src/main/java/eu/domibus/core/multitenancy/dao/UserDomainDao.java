package eu.domibus.core.multitenancy.dao;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface UserDomainDao {

    String findDomainByUser(String userName);

    String findPreferredDomainByUser(String userName);
    
    void setDomainByUser(String userName, String domainCode);
    
    void setPreferredDomainByUser(String userName, String domainCode);

    void deleteDomainByUser(String userName);

    List<UserDomainEntity> listPreferredDomains();

    List<String> listAllUserNames();
}
