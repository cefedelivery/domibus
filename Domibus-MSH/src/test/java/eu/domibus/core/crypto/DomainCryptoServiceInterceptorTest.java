package eu.domibus.core.crypto;

import eu.domibus.api.crypto.CryptoException;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.core.crypto.spi.CryptoSpiException;
import eu.domibus.core.crypto.spi.DomibusCertificateSpiException;
import eu.domibus.pki.DomibusCertificateException;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Assert;
import org.junit.Test;

import java.security.KeyStoreException;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class DomainCryptoServiceInterceptorTest {

    @Test
    public void convertCoreException() {

        DomainCryptoServiceInterceptor domainCryptoServiceInterceptor=new DomainCryptoServiceInterceptor();
        CryptoException transformedCryptoException = (CryptoException) domainCryptoServiceInterceptor.convertCoreException(new RuntimeException("bla"));
        Assert.assertEquals("[DOM_001]:bla",transformedCryptoException.getMessage());

        transformedCryptoException = (CryptoException) domainCryptoServiceInterceptor.convertCoreException(new CryptoSpiException("bla"));
        Assert.assertEquals("[DOM_001]:bla",transformedCryptoException.getMessage());

        DomibusCertificateException domibusCertificateException = (DomibusCertificateException) domainCryptoServiceInterceptor.convertCoreException(new DomibusCertificateSpiException("bla"));
        Assert.assertEquals("bla",domibusCertificateException.getMessage());

        final WSSecurityException wsSecurityException = new WSSecurityException(WSSecurityException.ErrorCode.SECURITY_ERROR);
        final WSSecurityException returnedWsSecurityException=(WSSecurityException) domainCryptoServiceInterceptor.convertCoreException(wsSecurityException);
        Assert.assertEquals(wsSecurityException,returnedWsSecurityException);

        final KeyStoreException keyStoreException = new KeyStoreException("test");
        final KeyStoreException returnedKeyStoreException=(KeyStoreException) domainCryptoServiceInterceptor.convertCoreException(keyStoreException);
        Assert.assertEquals(keyStoreException,returnedKeyStoreException);

        final CryptoException cryptoException = new CryptoException("test");
        final CryptoException returnedCryptoException=(CryptoException) domainCryptoServiceInterceptor.convertCoreException(cryptoException);
        Assert.assertEquals(cryptoException,returnedCryptoException);

        final ConfigurationException configurationException = new ConfigurationException("test");
        final ConfigurationException returnedConfigurationException=(ConfigurationException) domainCryptoServiceInterceptor.convertCoreException(configurationException);
        Assert.assertEquals(configurationException,returnedConfigurationException);
    }
}