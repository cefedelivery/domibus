package eu.domibus.common.services;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public interface DomibusCacheService {

    String USER_DOMAIN_CACHE = "userDomain";
    String PREFERRED_USER_DOMAIN_CACHE = "preferredUserDomain";
    String ALL_DOMAINS_CACHE = "allDomains";
    String DOMAIN_BY_CODE_CACHE = "domainByCode";
    String DOMAIN_BY_SCHEDULER_CACHE = "domainByScheduler";

    void clearCache(String refreshCacheName);

}
