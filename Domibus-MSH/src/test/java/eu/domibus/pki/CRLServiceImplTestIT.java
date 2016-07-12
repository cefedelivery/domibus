package eu.domibus.pki;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
import java.security.Security;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by Cosmin Baciu on 07-Jul-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CRLServiceImplTestIT.Config.class)
public class CRLServiceImplTestIT {

    @Configuration
    @EnableCaching
    static class Config {

        // Simulating the caching configuration
        @Bean
        SimpleCacheManager cacheManager() {
            SimpleCacheManager cacheManager = new SimpleCacheManager();
            List<Cache> caches = new ArrayList<Cache>();
            caches.add(crlByCertCacheBean().getObject());
            caches.add(crlByUrlCacheBean().getObject());
            cacheManager.setCaches(caches);
            return cacheManager;
        }

        @Bean
        public ConcurrentMapCacheFactoryBean crlByCertCacheBean() {
            ConcurrentMapCacheFactoryBean cacheFactoryBean = new ConcurrentMapCacheFactoryBean();
            cacheFactoryBean.setName("crlByCert");
            return cacheFactoryBean;
        }

        @Bean
        public ConcurrentMapCacheFactoryBean crlByUrlCacheBean() {
            ConcurrentMapCacheFactoryBean cacheFactoryBean = new ConcurrentMapCacheFactoryBean();
            cacheFactoryBean.setName("crlByUrl");
            return cacheFactoryBean;
        }

        @Bean
        CRLService crlService() {
            return new CRLServiceImpl();
        }

        @Bean
        CRLUtil crlUtil() {
            return Mockito.mock(CRLUtil.class);
        }
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @Autowired
    CRLUtil crlUtil;

    @Mock
    X509CRL crl;

    @Autowired
    CRLService crlService;

    @Autowired
    CacheManager cacheManager;

    @Test
    public void testIsCertificateRevoked() throws Exception {
        PKIUtil pkiUtil = new PKIUtil();
        X509Certificate certificate = pkiUtil.createCertificate(new BigInteger("0400000000011E44A5E405", 16), null);
        Mockito.when(crlUtil.getCrlDistributionPoints(certificate)).thenReturn(Arrays.asList(new String[]{"test.crl"}));
        Mockito.when(crlUtil.downloadCRL("test.crl")).thenReturn(crl);
        Mockito.when(crl.isRevoked(certificate)).thenReturn(true, false);

        boolean certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertTrue(certificateRevoked);

        //returns true when the certificate revoked status is cached
        certificateRevoked = crlService.isCertificateRevoked(certificate);
        assertTrue(certificateRevoked);
    }

    @Test
    public void testIsCertificateRevoked1() throws Exception {
        String serialNumber = "0400000000011E44A5E405";
        BigInteger serialNumberInteger = new BigInteger(serialNumber, 16);
        String crlUrl = "test.crl";

        Mockito.when(crlUtil.downloadCRL(crlUrl)).thenReturn(crl);
        Mockito.when(crlUtil.parseCertificateSerial(serialNumber)).thenReturn(serialNumberInteger);
        Set x509CRLEntries = new HashSet<>();
        X509CRLEntry x509CRLEntry = Mockito.mock(X509CRLEntry.class);
        x509CRLEntries.add(x509CRLEntry);
        Mockito.when(crl.getRevokedCertificates()).thenReturn(x509CRLEntries);

        Mockito.when(x509CRLEntry.getSerialNumber()).thenReturn(serialNumberInteger, BigInteger.ONE);

        boolean certificateRevoked = crlService.isCertificateRevoked(serialNumber, crlUrl);
        assertTrue(certificateRevoked);

        //returns true when the certificate revoked status is cached
        certificateRevoked = crlService.isCertificateRevoked(serialNumber, crlUrl);
        assertTrue(certificateRevoked);
    }

    @Test
    public void testChangeCacheExpirationTime() throws Exception {
        Object crlByCert = cacheManager.getCache("crlByCert").getNativeCache();
        System.out.println(crlByCert);

    }
}
