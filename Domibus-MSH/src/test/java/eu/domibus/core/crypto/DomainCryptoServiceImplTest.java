package eu.domibus.core.crypto;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.pki.CertificateService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import static org.apache.wss4j.common.ext.WSSecurityException.ErrorCode.SECURITY_ERROR;

/**
 * @author Sebastian-Ion TINCU
 */
public class DomainCryptoServiceImplTest {

    public static final String PRIVATE_KEY_PASSWORD = "privateKeyPassword";

    @Tested
    private DomainCryptoServiceImpl domainCryptoService;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected CertificateService certificateService;

    @Injectable
    protected SignalService signalService;

    @Injectable
    private X509Certificate x509Certificate;

    @Injectable
    private Domain domain;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(domain, "domibus.security.keystore.type"); result = "keystoreType";
            domibusPropertyProvider.getProperty(domain, "domibus.security.keystore.password"); result = "keystorePassword";
            domibusPropertyProvider.getProperty(domain, "domibus.security.key.private.alias"); result = "privateKeyAlias";
            domibusPropertyProvider.getProperty(domain, "domibus.security.key.private.password"); result = PRIVATE_KEY_PASSWORD;
            domibusPropertyProvider.getResolvedProperty(domain, "domibus.security.keystore.location"); result = "keystoreLocation";

            domibusPropertyProvider.getResolvedProperty(domain, "domibus.security.truststore.location"); result = "trustStoreLocation";
            domibusPropertyProvider.getProperty(domain, "domibus.security.truststore.password"); result = "trustStorePassword";
            domibusPropertyProvider.getProperty(domain, "domibus.security.truststore.type"); result = "trustStoreType";
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToLoadMerlinProperties_WSSecurityException(@Mocked Merlin merlin) throws WSSecurityException, IOException {
        // Given
        thrown.expect(CryptoException.class);
        thrown.expectMessage("Error loading properties");

        new Expectations() {{
            merlin.loadProperties((Properties) any, (ClassLoader) any, null); result = new WSSecurityException(SECURITY_ERROR);
        }};

        // When
        domainCryptoService.init();
    }

    @Test
    public void throwsExceptionWhenFailingToLoadMerlinProperties_IOException(@Mocked Merlin merlin) throws WSSecurityException, IOException {
        // Given
        thrown.expect(CryptoException.class);
        thrown.expectMessage("Error loading properties");

        new Expectations() {{
            merlin.loadProperties((Properties) any, (ClassLoader) any, null); result = new IOException();
        }};

        // When
        domainCryptoService.init();
    }

    @Test
    public void returnsKeystoreCertificateFromMerlin(@Mocked Merlin merlin, @Injectable KeyStore keyStore) throws Exception {
        // Given
        String alias = "alias";
        new Expectations() {{
            merlin.getKeyStore(); result = keyStore;
            keyStore.getCertificate(alias); result = x509Certificate;
        }};

        // When
        X509Certificate certificateFromKeyStore = domainCryptoService.getCertificateFromKeyStore(alias);

        // Then
        Assert.assertNotNull("Should have returned the keystore certificate from Merlin", certificateFromKeyStore);
    }


    @Test
    public void returnsTrustStoreCertificateFromMerlin(@Mocked Merlin merlin, @Injectable KeyStore trustStore) throws Exception {
        // Given
        String alias = "alias";
        new Expectations() {{
            merlin.getTrustStore(); result = trustStore;
            trustStore.getCertificate(alias); result = x509Certificate;
        }};

        // When
        X509Certificate certificateFromTrustStore = domainCryptoService.getCertificateFromTrustStore(alias);

        // Then
        Assert.assertNotNull("Should have returned the truststore certificate from Merlin", certificateFromTrustStore);
    }

    @Test
    public void returnsPrivateKeyPasswordAsTheValueOfThePropertyDefinedInTheCurrentDomain(@Mocked Merlin merlin) {
        // Given
        String alias = "alias";

        // When
        String privateKeyPassword = domainCryptoService.getPrivateKeyPassword(alias);

        // Then
        Assert.assertEquals("Should have returned the correct private key password", PRIVATE_KEY_PASSWORD, privateKeyPassword);
    }
}