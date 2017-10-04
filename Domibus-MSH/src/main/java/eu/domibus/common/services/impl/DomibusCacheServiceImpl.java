package eu.domibus.common.services.impl;

import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Service
public class DomibusCacheServiceImpl implements DomibusCacheService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheServiceImpl.class);
    @Autowired
    private CacheManager cacheManager;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void clearCache(String refreshCacheName) {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            if (StringUtils.equals(cacheName, refreshCacheName)) {
                final Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    LOG.debug("Clearing cache [" + refreshCacheName + "]");
                    cache.clear();
                }
            }
        }
    }
}
