package eu.domibus.core.cache;

import com.google.common.collect.Lists;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.NoOpCache;

import static org.junit.Assert.*;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class DomibusCacheServiceImplTest {

    @Tested
    private DomibusCacheServiceImpl domibusCacheService;

    @Injectable
    private CacheManager cacheManager;

    @Mocked
    private Cache cache;

    @Test
    public void doesNotRefreshTheCacheWhenTheCacheManagerContainsNoCaches() {
        new Expectations() {{
            cacheManager.getCacheNames(); result = Lists.<String>newArrayList();
        }};

        domibusCacheService.clearCache("cache");

        new Verifications() {{
            cache.clear(); times = 0;
        }};
    }

    @Test
    public void doesNotRefreshTheCacheWhenTheCacheManagerContainsOnlyNonMatchingCaches() {
        new Expectations() {{
            cacheManager.getCacheNames(); result = Lists.newArrayList("nonMatching");
        }};

        domibusCacheService.clearCache("cache");

        new Verifications() {{
            cache.clear(); times = 0;
        }};
    }

    @Test
    public void doesNotRefreshTheCacheWhenTheCacheManagerContainsNullMatchingCache() {
        new Expectations() {{
            cacheManager.getCacheNames(); result = Lists.newArrayList("cache");
            cacheManager.getCache("cache"); result = null;
        }};

        domibusCacheService.clearCache("cache");

        new Verifications() {{
            cache.clear(); times = 0;
        }};
    }

    @Test
    public void refreshTheCacheWhenTheCacheManagerContainsMatchingCache() {
        new Expectations() {{
            cacheManager.getCacheNames(); result = Lists.newArrayList("cache");
            cacheManager.getCache("cache"); result = cache;
        }};

        domibusCacheService.clearCache("cache");

        new Verifications() {{
            cache.clear(); times = 1;
        }};
    }
}