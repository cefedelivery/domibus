package eu.domibus.pki;

import eu.domibus.wss4j.common.crypto.TrustStoreService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheFactoryBean;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigInteger;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by Cosmin Baciu on 07-Jul-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CertificateServiceImplTestIT.Config.class)
public class CertificateServiceImplTestIT {

    @Configuration
    @EnableCaching
//    @ComponentScan(basePackages = {"eu.domibus.pki"}, excludeFilters={
//            @ComponentScan.Filter(type= FilterType.ASSIGNABLE_TYPE, value=TrustStoreServiceImpl.class)})
    static class Config {

        // Simulating the caching configuration
        @Bean
        SimpleCacheManager cacheManager() {
            SimpleCacheManager cacheManager = new SimpleCacheManager();
            List<Cache> caches = new ArrayList<Cache>();
            caches.add(crlByCertCacheBean().getObject());
            cacheManager.setCaches(caches);
            return cacheManager;
        }

        @Bean
        public ConcurrentMapCacheFactoryBean crlByCertCacheBean() {
            ConcurrentMapCacheFactoryBean cacheFactoryBean = new ConcurrentMapCacheFactoryBean();
            cacheFactoryBean.setName("certValidationByAlias");
            return cacheFactoryBean;
        }

        @Bean
        TrustStoreService trustStoreService() {
            return Mockito.mock(TrustStoreService.class);
        }

        @Bean
        CRLService crlService() {
            return Mockito.mock(CRLService.class);
        }

        @Bean
        CertificateService certificateService() {
            return new CertificateServiceImpl();
        }
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Autowired
    CRLService crlService;

    @Autowired
    CertificateService certificateService;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    TrustStoreService trustStoreService;

//    @Mock
//    KeyStore trustStore;

    @Test
    public void testIsCertificateRevoked() throws Exception {
        final String receiverAlias = "red_gw";
        PKIUtil pkiUtil = new PKIUtil();
        final X509Certificate rootCertificate = pkiUtil.createCertificate(BigInteger.ONE, null);
        final X509Certificate receiverCertificate = pkiUtil.createCertificate(BigInteger.ONE, null);

        KeyStore trustStore = Mockito.mock(KeyStore.class);
        Mockito.when(trustStoreService.getTrustStore()).thenReturn(trustStore);
        X509Certificate[] certificateChain = new X509Certificate[]{receiverCertificate, rootCertificate};
        Mockito.doReturn(certificateChain).when(trustStore).getCertificateChain(receiverAlias);
//        Mockito.when(trustStore.getCertificateChain(receiverAlias)).thenReturn(certificateChain);

        Mockito.when(certificateService.isCertificateValid(rootCertificate)).thenReturn(true);
        Mockito.when(certificateService.isCertificateValid(receiverCertificate)).thenReturn(true, false);

        boolean certificateRevoked = certificateService.isCertificateChainValid(receiverAlias);
        assertTrue(certificateRevoked);

        certificateRevoked = certificateService.isCertificateChainValid(receiverAlias);
        assertTrue(certificateRevoked);
    }


}
