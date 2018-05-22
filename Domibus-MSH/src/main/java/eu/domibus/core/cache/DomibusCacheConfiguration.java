package eu.domibus.core.cache;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
public class DomibusCacheConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusCacheConfiguration.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Value("${domibus.config.location}/internal/ehcache.xml")
    protected String externalEhCacheFile;

    @Bean(name = "cacheManager")
    public EhCacheCacheManager ehCacheManager(@Autowired CacheManager cacheManager) {
        EhCacheCacheManager ehCacheManager = new EhCacheCacheManager();
        ehCacheManager.setCacheManager(cacheManager);

        if (new File(externalEhCacheFile).exists()) {
            LOG.debug("External ehCache file exists [{}]. Overriding the default ehCache configuration", externalEhCacheFile);
            CacheManager externalCacheManager = CacheManager.newInstance(externalEhCacheFile);
            final String[] cacheNames = externalCacheManager.getCacheNames();
            for (String cacheName : cacheNames) {
                if (cacheManager.cacheExists(cacheName)) {
                    LOG.debug("Overriding the default cache [{}]", cacheName);
                    cacheManager.removeCache(cacheName);
                    final CacheConfiguration cacheConfiguration = externalCacheManager.getCache(cacheName).getCacheConfiguration();
                    cacheManager.addCache(new Cache(cacheConfiguration));
                }
            }
        }

        return ehCacheManager;
    }


    @Bean(name = "ehcache")
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        final ClassPathResource classPathResource = new ClassPathResource("config/ehcache/ehcache-default.xml");
        ehCacheManagerFactoryBean.setConfigLocation(classPathResource);
        ehCacheManagerFactoryBean.setCacheManagerName("default");
        return ehCacheManagerFactoryBean;
    }

}
