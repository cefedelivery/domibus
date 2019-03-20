package eu.domibus.core.crypto;

import com.google.common.collect.Lists;
import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.crypto.CryptoException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.core.crypto.api.CertificateEntry;
import eu.domibus.pki.CertificateService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.wss4j.common.crypto.PasswordEncryptor;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @author Sebastian-Ion TINCU
 */
@RunWith(JMockit.class)
public class DomainCryptoServiceImplNonInitializedTest {

    public static final String PRIVATE_KEY_PASSWORD = "privateKeyPassword";

    public static final String TRUST_STORE_PASSWORD = "trustStorePassword";

    public static final String TRUST_STORE_TYPE = "trustStoreType";

    public static final String TRUST_STORE_LOCATION = "trustStoreLocation";

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

    @Injectable
    private KeyStore trustStore;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() {
        new MockUp<DomainCryptoServiceImpl>() {
            @Mock
            void init() { /* avoid @PostConstruct initialization */ }
        };

        new NonStrictExpectations() {{
            domibusPropertyProvider.getProperty(domain, "domibus.security.keystore.type"); result = "keystoreType";
            domibusPropertyProvider.getProperty(domain, "domibus.security.keystore.password"); result = "keystorePassword";
            domibusPropertyProvider.getProperty(domain, "domibus.security.key.private.alias"); result = "privateKeyAlias";
            domibusPropertyProvider.getProperty(domain, "domibus.security.key.private.password"); result = PRIVATE_KEY_PASSWORD;
            domibusPropertyProvider.getResolvedProperty(domain, "domibus.security.keystore.location"); result = "keystoreLocation";

            domibusPropertyProvider.getResolvedProperty(domain, "domibus.security.truststore.location"); result = TRUST_STORE_LOCATION;
            domibusPropertyProvider.getProperty(domain, "domibus.security.truststore.password"); result = "trustStorePassword";
            domibusPropertyProvider.getProperty(domain, "domibus.security.truststore.type"); result = TRUST_STORE_TYPE;
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToBackupTheCurrentTrustStore_IOException(@Mocked ByteArrayOutputStream oldTrustStoreBytes) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not replace truststore");

        new Expectations() {{
            new ByteArrayOutputStream(); result = oldTrustStoreBytes;
            trustStore.store(oldTrustStoreBytes, (char[]) any); result = new IOException();
        }};

        // When
        domainCryptoService.replaceTrustStore(new byte[]{}, "");

        new Verifications() {{
            oldTrustStoreBytes.close();
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToBackupTheCurrentTrustStore_NoSuchAlgorithmException(@Mocked ByteArrayOutputStream oldTrustStoreBytes) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not replace truststore");

        new Expectations() {{
            new ByteArrayOutputStream(); result = oldTrustStoreBytes;
            trustStore.store(oldTrustStoreBytes, (char[]) any); result = new NoSuchAlgorithmException();
        }};

        // When
        domainCryptoService.replaceTrustStore(new byte[]{}, "");

        new Verifications() {{
            oldTrustStoreBytes.close();
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToBackupTheCurrentTrustStore_CertificateException(@Mocked ByteArrayOutputStream oldTrustStoreBytes) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not replace truststore");

        new Expectations() {{
            new ByteArrayOutputStream(); result = oldTrustStoreBytes;
            trustStore.store(oldTrustStoreBytes, (char[]) any); result = new CertificateException();
        }};

        // When
        domainCryptoService.replaceTrustStore(new byte[]{}, "");

        new Verifications() {{
            oldTrustStoreBytes.close();
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToBackupTheCurrentTrustStore_KeyStoreException(@Mocked ByteArrayOutputStream oldTrustStoreBytes) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not replace truststore");

        new Expectations() {{
            new ByteArrayOutputStream(); result = oldTrustStoreBytes;
            trustStore.store(oldTrustStoreBytes, (char[]) any); result = new KeyStoreException();
        }};

        // When
        domainCryptoService.replaceTrustStore(new byte[]{}, "");

        new Verifications() {{
            oldTrustStoreBytes.close();
        }};
    }

    @Test
    public void verifiesTheNewTruststoreCanBeLoadedUsingTheProvidedPasswordBeforeReplacingTheOldTruststore(@Mocked ByteArrayInputStream newTrustStoreBytes) throws Exception {
        // Given
        byte[] store = {1, 2, 3};
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        new MockUp<DomainCryptoServiceImpl>() { @Mock void persistTrustStore() {/* ignore */} };
        new Expectations() {{
            new ByteArrayInputStream(store); result = newTrustStoreBytes;
        }};

        // When
        domainCryptoService.replaceTrustStore(store, TRUST_STORE_PASSWORD);

        new Verifications() {{
            certificateService.validateLoadOperation(newTrustStoreBytes, TRUST_STORE_PASSWORD, TRUST_STORE_TYPE);
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToLoadTheNewTrustStore_IOException(@Mocked ByteArrayInputStream newTrustStoreBytes) throws Exception {
        // Given
        byte[] store = {1, 2, 3};
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("originalMessage");

        new MockUp<DomainCryptoServiceImpl>() { @Mock void persistTrustStore() {/* ignore */} };
        new Expectations() {{
            new ByteArrayInputStream(store); result = newTrustStoreBytes;
            trustStore.load(newTrustStoreBytes, (char[]) any); result = new IOException("originalMessage");
        }};

        // When
        domainCryptoService.replaceTrustStore(store, "");
    }

    @Test
    public void throwsExceptionWhenFailingToLoadTheNewTrustStore_NoSuchAlgorithmException(@Mocked ByteArrayInputStream newTrustStoreBytes) throws Exception {
        // Given
        byte[] store = {1, 2, 3};
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("originalMessage");

        new MockUp<DomainCryptoServiceImpl>() { @Mock void persistTrustStore() {/* ignore */} };
        new Expectations() {{
            new ByteArrayInputStream(store); result = newTrustStoreBytes;
            trustStore.load(newTrustStoreBytes, (char[]) any); result = new NoSuchAlgorithmException("originalMessage");
        }};

        // When
        domainCryptoService.replaceTrustStore(store, "");
    }

    @Test
    public void throwsExceptionWhenFailingToLoadTheNewTrustStore_CertificateException(@Mocked ByteArrayInputStream newTrustStoreBytes) throws Exception {
        // Given
        byte[] store = {1, 2, 3};
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("originalMessage");

        new MockUp<DomainCryptoServiceImpl>() { @Mock void persistTrustStore() {/* ignore */} };
        new Expectations() {{
            new ByteArrayInputStream(store); result = newTrustStoreBytes;
            trustStore.load(newTrustStoreBytes, (char[]) any); result = new CertificateException("originalMessage");
        }};

        // When
        domainCryptoService.replaceTrustStore(store, "");
    }

    @Test
    public void throwsExceptionWhenPersistTheTrustStore_CryptoException(@Mocked ByteArrayInputStream newTrustStoreBytes) throws Exception {
        // Given
        byte[] store = {1, 2, 3};
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("originalMessage");

        new MockUp<DomainCryptoServiceImpl>() { @Mock void persistTrustStore() {
            throw new CryptoException("originalMessage");
        } };
        new Expectations() {{
            new ByteArrayInputStream(store); result = newTrustStoreBytes;
        }};

        // When
        domainCryptoService.replaceTrustStore(store, "");
    }

    @Test
    public void throwsExceptionWhenFailingToRestoreTheOldTrustStoreInCaseOfAnInitialFailureWhenLoadingTheNewTrustStore_IOException(
            @Mocked ByteArrayOutputStream oldTrustStoreBytes, @Injectable InputStream oldTrustStoreInputStream, @Mocked ByteArrayInputStream newTrustStoreBytes
            ) throws Exception {
        // Given
        byte[] store = {1, 2, 3};
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not replace truststore and old truststore was not reverted properly. Please correct the error before continuing.");

        new MockUp<DomainCryptoServiceImpl>() { @Mock void persistTrustStore() {/* ignore */} };
        new Expectations() {{
            new ByteArrayOutputStream(); result = oldTrustStoreBytes;
            new ByteArrayInputStream(store); result = newTrustStoreBytes;

            oldTrustStoreBytes.toInputStream(); result = oldTrustStoreInputStream;

            trustStore.load(newTrustStoreBytes, (char[]) any); result = new IOException("originalMessage");
            trustStore.load(oldTrustStoreInputStream, (char[]) any); result = new IOException();
        }};

        // When
        domainCryptoService.replaceTrustStore(store, "");
    }

    @Test
    public void throwsExceptionWhenFailingToRestoreTheOldTrustStoreInCaseOfAnInitialFailureWhenLoadingTheNewTrustStore_NoSuchAlgorithmException(
            @Mocked ByteArrayOutputStream oldTrustStoreBytes, @Injectable InputStream oldTrustStoreInputStream, @Mocked ByteArrayInputStream newTrustStoreBytes
            ) throws Exception {
        // Given
        byte[] store = {1, 2, 3};
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not replace truststore and old truststore was not reverted properly. Please correct the error before continuing.");

        new MockUp<DomainCryptoServiceImpl>() { @Mock void persistTrustStore() {/* ignore */} };
        new Expectations() {{
            new ByteArrayOutputStream(); result = oldTrustStoreBytes;
            new ByteArrayInputStream(store); result = newTrustStoreBytes;

            oldTrustStoreBytes.toInputStream(); result = oldTrustStoreInputStream;

            trustStore.load(newTrustStoreBytes, (char[]) any); result = new IOException("originalMessage");
            trustStore.load(oldTrustStoreInputStream, (char[]) any); result = new NoSuchAlgorithmException();
        }};

        // When
        domainCryptoService.replaceTrustStore(store, "");
    }

    @Test
    public void throwsExceptionWhenFailingToRestoreTheOldTrustStoreInCaseOfAnInitialFailureWhenLoadingTheNewTrustStore_CertificateException(
            @Mocked ByteArrayOutputStream oldTrustStoreBytes, @Injectable InputStream oldTrustStoreInputStream, @Mocked ByteArrayInputStream newTrustStoreBytes
            ) throws Exception {
        // Given
        byte[] store = {1, 2, 3};
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not replace truststore and old truststore was not reverted properly. Please correct the error before continuing.");

        new MockUp<DomainCryptoServiceImpl>() { @Mock void persistTrustStore() {/* ignore */} };
        new Expectations() {{
            new ByteArrayOutputStream(); result = oldTrustStoreBytes;
            new ByteArrayInputStream(store); result = newTrustStoreBytes;

            oldTrustStoreBytes.toInputStream(); result = oldTrustStoreInputStream;

            trustStore.load(newTrustStoreBytes, (char[]) any); result = new IOException("originalMessage");
            trustStore.load(oldTrustStoreInputStream, (char[]) any); result = new CertificateException();
        }};

        // When
        domainCryptoService.replaceTrustStore(store, "");
    }

    @Test(expected = CryptoException.class) // ignore the CryptoException being initially thrown
    public void signalsTheTrustStoreUpdateWhenSuccessfullyRestoringTheOldTrustStoreInCaseOfAnInitialFailureWhenLoadingTheNewTrustStore(
            @Mocked ByteArrayOutputStream oldTrustStoreBytes, @Injectable InputStream oldTrustStoreInputStream, @Mocked ByteArrayInputStream newTrustStoreBytes
            ) throws Exception {
        // Given
        byte[] store = {1, 2, 3};
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        new MockUp<DomainCryptoServiceImpl>() { @Mock void persistTrustStore() {/* ignore */} };
        new Expectations() {{
            new ByteArrayOutputStream(); result = oldTrustStoreBytes;
            new ByteArrayInputStream(store); result = newTrustStoreBytes;

            oldTrustStoreBytes.toInputStream(); result = oldTrustStoreInputStream;

            trustStore.load(newTrustStoreBytes, (char[]) any); result = new IOException();
            trustStore.load(oldTrustStoreInputStream, (char[]) any);
        }};

        // When
        domainCryptoService.replaceTrustStore(store, "");

        // Then
        new Verifications() {{
            signalService.signalTrustStoreUpdate(domain);
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToCreateMissingParentFolderForPersistingTheTrustStore(@Injectable File trustStoreFile, @Injectable File trustStoreDirectory) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not create parent directory for truststore");

        new MockUp<FileUtils>() {
            @Mock
            void forceMkdir(File directory) throws IOException {
                throw new IOException();
            }
        };

        new Expectations(File.class) {{
            new File(TRUST_STORE_LOCATION); result = trustStoreFile;
            trustStoreFile.getParentFile(); result = trustStoreDirectory;
            trustStoreDirectory.exists(); result = false;
        }};

        // When
        Deencapsulation.invoke(domainCryptoService, "persistTrustStore");
    }

    @Test
    public void createsMissingParentDirectoryWhenPersistingTheTrustStore(@Injectable File trustStoreFile, @Injectable File trustStoreDirectory,
            @Mocked FileUtils fileUtils) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        new MockUp<FileOutputStream>() {
            @Mock void $init(File file) { /* ignore */ }
            @Mock void close() { /* ignore */ }
        };

        new Expectations(File.class) {{
            new File(TRUST_STORE_LOCATION); result = trustStoreFile;
            trustStoreFile.getParentFile(); result = trustStoreDirectory;
            trustStoreDirectory.exists(); result = false;

            trustStoreFile.getAbsolutePath(); result = "";
        }};
        // When
        Deencapsulation.invoke(domainCryptoService, "persistTrustStore");

        // Then
        new Verifications() {{
            FileUtils.forceMkdir(trustStoreDirectory);
        }};
    }

    @Test
    public void storesTheTrustStoreContentCorrectlyWhenPersistingTheTrustStore(@Injectable File trustStoreFile, @Injectable File trustStoreDirectory) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        // use fakes since we cannot mock most of the java.io classes (see mockit.internal.expectations.mocking.ExpectationsModifier
        // #isDisallowedJREClass(String)) and partial mocking for FileOutputStream via @Injectable fails when calling #close()
        new MockUp<FileOutputStream>() {
            @Mock void $init(Invocation invocation, File file) {
                if(file != trustStoreFile) {
                    invocation.proceed();
                }
            }
            @Mock void close() { /* ignore */}
        };

        new Expectations(File.class) {{
            new File(TRUST_STORE_LOCATION); result = trustStoreFile;
            trustStoreFile.getParentFile(); result = trustStoreDirectory;
            trustStoreDirectory.exists(); result = true;

            trustStoreFile.getAbsolutePath(); result = "";
        }};

        // When
        Deencapsulation.invoke(domainCryptoService, "persistTrustStore");

        // Then
        new Verifications() {{
            trustStore.store((FileOutputStream) any, (char[]) any);
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToPersistTheTrustStore_IOException(@Injectable File trustStoreFile, @Injectable File trustStoreDirectory) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not persist truststore:");

        new MockUp<FileOutputStream>() {
            @Mock void $init(File file) { /* ignore */ }
            @Mock void close() { /* ignore */}
        };

        new Expectations(File.class) {{
            new File(TRUST_STORE_LOCATION); result = trustStoreFile;
            trustStoreFile.getParentFile(); result = trustStoreDirectory;
            trustStoreDirectory.exists(); result = true;

            trustStoreFile.getAbsolutePath(); result = "";
            trustStore.store((FileOutputStream) any, (char[]) any); result = new IOException();
        }};

        // When
        Deencapsulation.invoke(domainCryptoService, "persistTrustStore");
    }

    @Test
    public void throwsExceptionWhenFailingToPersistTheTrustStore_NoSuchAlgorithmException(@Injectable File trustStoreFile, @Injectable File trustStoreDirectory) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not persist truststore:");

        new MockUp<FileOutputStream>() {
            @Mock void $init(File file) { /* ignore */ }
            @Mock void close() { /* ignore */}
        };

        new Expectations(File.class) {{
            new File(TRUST_STORE_LOCATION); result = trustStoreFile;
            trustStoreFile.getParentFile(); result = trustStoreDirectory;
            trustStoreDirectory.exists(); result = true;

            trustStoreFile.getAbsolutePath(); result = "";
            trustStore.store((FileOutputStream) any, (char[]) any); result = new NoSuchAlgorithmException();
        }};

        // When
        Deencapsulation.invoke(domainCryptoService, "persistTrustStore");
    }

    @Test
    public void throwsExceptionWhenFailingToPersistTheTrustStore_CertificateException(@Injectable File trustStoreFile, @Injectable File trustStoreDirectory) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not persist truststore:");

        new MockUp<FileOutputStream>() {
            @Mock void $init(File file) { /* ignore */ }
            @Mock void close() { /* ignore */}
        };

        new Expectations(File.class) {{
            new File(TRUST_STORE_LOCATION); result = trustStoreFile;
            trustStoreFile.getParentFile(); result = trustStoreDirectory;
            trustStoreDirectory.exists(); result = true;

            trustStoreFile.getAbsolutePath(); result = "";
            trustStore.store((FileOutputStream) any, (char[]) any); result = new CertificateException();
        }};

        // When
        Deencapsulation.invoke(domainCryptoService, "persistTrustStore");
    }

    @Test
    public void throwsExceptionWhenFailingToPersistTheTrustStore_KeyStoreException(@Injectable File trustStoreFile, @Injectable File trustStoreDirectory) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not persist truststore:");

        new MockUp<FileOutputStream>() {
            @Mock void $init(File file) { /* ignore */ }
            @Mock void close() { /* ignore */}
        };

        new Expectations(File.class) {{
            new File(TRUST_STORE_LOCATION); result = trustStoreFile;
            trustStoreFile.getParentFile(); result = trustStoreDirectory;
            trustStoreDirectory.exists(); result = true;

            trustStoreFile.getAbsolutePath(); result = "";
            trustStore.store((FileOutputStream) any, (char[]) any); result = new KeyStoreException();
        }};

        // When
        Deencapsulation.invoke(domainCryptoService, "persistTrustStore");
    }

    @Test
    public void signalsTheTrustStoreUpdateWhenSuccessfullyPersisting(@Injectable File trustStoreFile, @Injectable File trustStoreDirectory) throws Exception {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        new MockUp<FileOutputStream>() {
            @Mock void $init(File file) { /* ignore */ }
            @Mock void close() { /* ignore */}
        };

        new Expectations(File.class) {{
            new File(TRUST_STORE_LOCATION); result = trustStoreFile;
            trustStoreFile.getParentFile(); result = trustStoreDirectory;
            trustStoreDirectory.exists(); result = true;

            trustStoreFile.getAbsolutePath(); result = "";
        }};

        // When
        Deencapsulation.invoke(domainCryptoService, "persistTrustStore");

        // Then
        new Verifications() {{
            signalService.signalTrustStoreUpdate(domain);
        }};
    }

    @Test
    public void returnsTheCorrectValidityOfTheCertificateChain() {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        new Expectations() {{
            certificateService.isCertificateChainValid(trustStore, "alias"); result = true;
        }};

        // When
        boolean valid = domainCryptoService.isCertificateChainValid("alias");

        // Then
        Assert.assertTrue("Should have correctly returned the validity of the certificate chain", valid);
    }

    @Test
    public void throwsExceptionWhenAddingCertificateIntoTheTrustStoreButFailingToCheckThePresenceOfItsAlias(@Injectable X509Certificate certificate) throws KeyStoreException {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Error while trying to get the alias from the truststore. This should never happen");

        new Expectations() {{
            trustStore.containsAlias("alias"); result = new KeyStoreException();
        }};

        // When
        domainCryptoService.addCertificate(certificate, "alias", true);
    }

    @Test
    public void returnsFalseWhenAddingExistingCertificateIntoTheTrustStoreWithoutIntentionOfOverwritingIt(@Injectable X509Certificate certificate) throws KeyStoreException {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        new MockUp<DomainCryptoServiceImpl>() {
            @Mock void persistTrustStore() { /* ignore */ }
        };

        new Expectations() {{
            trustStore.containsAlias("alias"); result = true;
        }};

        // When
        boolean result = domainCryptoService.addCertificate(certificate, "alias", false);

        // Then
        Assert.assertFalse("Should have returned false when adding an existing certificate to the trust store without the intention of overwriting it", result);
    }

    @Test
    public void overwritesTrustedCertificateWhenAddingExistingCertificateIntoTheTrustStoreWithIntentionOfOverwritingIt(@Injectable X509Certificate certificate) throws KeyStoreException {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        new MockUp<DomainCryptoServiceImpl>() {
            @Mock void persistTrustStore() { /* ignore */ }
        };

        new Expectations() {{
            trustStore.containsAlias("alias"); result = true;
        }};

        // When
        boolean result = domainCryptoService.addCertificate(certificate, "alias", true);

        // Then
        Assert.assertTrue("Should have returned true when adding an existing certificate to the trust store with the intention of overwriting it", result);
        new Verifications() {{
           trustStore.deleteEntry("alias");
           trustStore.setCertificateEntry("alias", certificate);
        }};
    }

    @Test
    public void insertsTrustedCertificateWhenAddingNonExistingCertificateIntoTheTrustStore(@Injectable X509Certificate certificate) throws KeyStoreException {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        new MockUp<DomainCryptoServiceImpl>() {
            @Mock void persistTrustStore() { /* ignore */ }
        };

        new Expectations() {{
            trustStore.containsAlias("alias"); result = false;
        }};

        // When
        boolean result = domainCryptoService.addCertificate(certificate, "alias", true);

        // Then
        Assert.assertTrue("Should have returned true when adding a non-existing certificate to the trust store", result);
        new Verifications() {{
            trustStore.deleteEntry("alias"); times = 0;
            trustStore.setCertificateEntry("alias", certificate);
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToOverwriteExistingCertificateIntoTheTrustStore(@Injectable X509Certificate certificate) throws KeyStoreException {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(ConfigurationException.class);

        new MockUp<DomainCryptoServiceImpl>() {
            @Mock void persistTrustStore() { /* ignore */ }
        };

        new Expectations() {{
            trustStore.containsAlias("alias"); result = true;
            trustStore.deleteEntry("alias"); result = new KeyStoreException();
        }};

        // When
        domainCryptoService.addCertificate(certificate, "alias", true);
    }

    @Test
    public void throwsExceptionWhenFailingToStoreTrustedCertificateIntoTheTrustStore(@Injectable X509Certificate certificate) throws KeyStoreException {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(ConfigurationException.class);

        new Expectations() {{
            trustStore.containsAlias("alias"); result = false;
            trustStore.setCertificateEntry("alias", certificate); result = new KeyStoreException();
        }};

        // When
        domainCryptoService.addCertificate(certificate, "alias", true);
    }

    @Test
    public void persistsTheTrustStoreAfterAddingCertificate(@Injectable X509Certificate certificate) {
        // Given
        new MockUp<DomainCryptoServiceImpl>() {
            int count = 0;

            @Mock boolean addCertificate(Invocation invocation, X509Certificate x509Certificate, String alias, boolean overwrite) {
                invocation.proceed();
                Assert.assertEquals("Should have persisted the trust store after adding or replacing certificates", 1, count);
                return true;
            }

            @Mock boolean doAddCertificate(X509Certificate certificate, String alias, boolean overwrite) {
                return true;
            }

            @Mock void persistTrustStore(Invocation invocation) {
                count = invocation.getInvocationCount();
            }
        };

        // When
        domainCryptoService.addCertificate(certificate, "alias", true);

    }

    @Test
    public void doesNotPersistTheTrustStoreWhenAddingCertificateThatDoesNotAlterItsContent(@Injectable X509Certificate certificate) {
        // Given
        new MockUp<DomainCryptoServiceImpl>() {
            @Mock boolean doAddCertificate(X509Certificate certificate, String alias, boolean overwrite) {
                return false;
            }

            @Mock void persistTrustStore() {
                Assert.fail("Should have not persisted the trust store if not adding nor replacing certificates inside");
            }
        };

        // When
        domainCryptoService.addCertificate(certificate, "alias", false);
    }

    @Test
    public void addsMultipleCertificatesIntoTheTrustStore(@Injectable X509Certificate firstCertificate, @Injectable X509Certificate secondCertificate,
            @Injectable CertificateEntry first, @Injectable CertificateEntry second) {
        // Given
        new MockUp<DomainCryptoServiceImpl>() {
            List<CertificateEntry> result = Lists.newArrayList();

            @Mock boolean addCertificate(Invocation invocation, List<CertificateEntry> certificates, boolean overwrite) {
                invocation.proceed();
                Assert.assertTrue("Should have added all certificates into the trust store",
                        result.size() == 2
                                && result.stream().allMatch(certificate -> certificate.getCertificate() == firstCertificate && certificate.getAlias().equals("first")
                                            || certificate.getCertificate() == secondCertificate && certificate.getAlias().equals("second")));
                return true;
            }

            @Mock boolean doAddCertificate(X509Certificate certificate, String alias, boolean overwrite) {
                result.add(new CertificateEntry(alias, certificate));
                return true;
            }

            @Mock void persistTrustStore(Invocation invocation) { /* ignore */ }
        };

        new Expectations() {{
           first.getCertificate(); result = firstCertificate;
           first.getAlias(); result = "first";
           second.getCertificate(); result = secondCertificate;
           second.getAlias(); result = "second";
        }};

        // When
        domainCryptoService.addCertificate(Lists.newArrayList(first, second), false);
    }


    @Test
    public void persistsTheTrustStoreAfterAddingMultipleCertificates(@Injectable CertificateEntry first, @Injectable CertificateEntry second) {
        // Given
        new MockUp<DomainCryptoServiceImpl>() {
            int count = 0;
            @Mock boolean addCertificate(Invocation invocation, List<CertificateEntry> certificates, boolean overwrite) {
                invocation.proceed();
                Assert.assertEquals("Should have persisted the trust store after adding or replacing multiple certificates", 1, count);
                return true;
            }

            @Mock boolean doAddCertificate(X509Certificate certificate, String alias, boolean overwrite) {
                return true;
            }

            @Mock void persistTrustStore(Invocation invocation) {
                count = invocation.getInvocationCount();
            }
        };

        // When
        domainCryptoService.addCertificate(Lists.newArrayList(first, second), false);
    }

    @Test
    public void throwsExceptionWhenRemovingCertificateFromTheTrustStoreButFailingToCheckThePresenceOfItsAlias() throws KeyStoreException {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(CryptoException.class);
        thrown.expectMessage("Error while trying to get the alias from the truststore. This should never happen");

        new Expectations() {{
            trustStore.containsAlias("alias"); result = new KeyStoreException();
        }};

        // When
        domainCryptoService.removeCertificate("alias");
    }

    @Test
    public void doesNothingWhenRemovingNonExistingCertificateFromTheTrustStore() throws KeyStoreException {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        new MockUp<DomainCryptoServiceImpl>() {
            @Mock void persistTrustStore() { /* ignore */ }
        };

        new Expectations() {{
            trustStore.containsAlias("alias"); result = false;
        }};

        // When
        boolean result = domainCryptoService.removeCertificate("alias");

        // Then
        Assert.assertFalse("Should have returned false when removing a non-existing certificate from the trust store", result);
    }

    @Test
    public void removesTrustedCertificateWhenRemovingExistingCertificateFromTheTrustStore() throws KeyStoreException {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        new MockUp<DomainCryptoServiceImpl>() {
            @Mock void persistTrustStore() { /* ignore */ }
        };

        new Expectations() {{
            trustStore.containsAlias("alias"); result = true;
        }};

        // When
        boolean result = domainCryptoService.removeCertificate("alias");

        // Then
        Assert.assertTrue("Should have returned true when removing an existing certificate from the trust store", result);
        new Verifications() {{
            trustStore.deleteEntry("alias");
        }};
    }

    @Test
    public void throwsExceptionWhenFailingToRemoveExistingCertificateFromTheTrustStore() throws KeyStoreException {
        // Given
        Deencapsulation.setField(domainCryptoService, "truststore", trustStore);

        thrown.expect(ConfigurationException.class);

        new MockUp<DomainCryptoServiceImpl>() {
            @Mock void persistTrustStore() { /* ignore */ }
        };

        new Expectations() {{
            trustStore.containsAlias("alias"); result = true;
            trustStore.deleteEntry("alias"); result = new KeyStoreException();
        }};

        // When
        domainCryptoService.removeCertificate("alias");
    }

    @Test
    public void persistsTheTrustStoreAfterRemovingCertificate() {
        // Given
        new MockUp<DomainCryptoServiceImpl>() {
            int count = 0;

            @Mock boolean removeCertificate(Invocation invocation, String alias) {
                invocation.proceed();
                Assert.assertEquals("Should have persisted the trust store after removing certificates", 1, count);
                return true;
            }

            @Mock boolean doRemoveCertificate(String alias) {
                return true;
            }

            @Mock void persistTrustStore(Invocation invocation) {
                count = invocation.getInvocationCount();
            }
        };

        // When
        domainCryptoService.removeCertificate("alias");

    }

    @Test
    public void doesNotPersistTheTrustStoreWhenRemovingCertificateThatDoesNotAlterItsContent(@Injectable X509Certificate certificate) {
        // Given
        new MockUp<DomainCryptoServiceImpl>() {
            @Mock boolean doRemoveCertificate(String alias) {
                return false;
            }

            @Mock void persistTrustStore() {
                Assert.fail("Should have not persisted the trust store if not removing certificates inside");
            }
        };

        // When
        domainCryptoService.removeCertificate("alias");
    }

    @Test
    public void removeMultipleCertificatesIntoTheTrustStore() {
        // Given
        new MockUp<DomainCryptoServiceImpl>() {
            List<String> result = Lists.newArrayList();

            @Mock boolean removeCertificate(Invocation invocation, List<String> aliases) {
                invocation.proceed();
                Assert.assertTrue("Should have removed all certificates from the trust store",
                        result.size() == 2 && result.containsAll(Lists.newArrayList("first", "second")));
                return true;
            }

            @Mock boolean doRemoveCertificate(String alias) {
                result.add(alias);
                return true;
            }

            @Mock void persistTrustStore(Invocation invocation) { /* ignore */}
        };
        // When
        domainCryptoService.removeCertificate(Lists.newArrayList("first", "second"));
    }

    @Test
    public void persistsTheTrustStoreAfterRemovingMultipleCertificates() {
        // Given
        new MockUp<DomainCryptoServiceImpl>() {
            int count = 0;
            @Mock boolean removeCertificate(Invocation invocation, List<String> aliases) {
                invocation.proceed();
                Assert.assertEquals("Should have persisted the trust store after removing multiple certificates", 1, count);
                return true;
            }

            @Mock boolean doRemoveCertificate(String alias) {
                return true;
            }

            @Mock void persistTrustStore(Invocation invocation) {
                count = invocation.getInvocationCount();
            }
        };

        // When
        domainCryptoService.removeCertificate(Lists.newArrayList("first", "second"));
    }

    @Test
    public void returnsTheCorrectTrustStoreWhenLoadingIt(@Injectable InputStream trustStoreInputStream) {
        // Given
        new MockUp<DomainCryptoServiceImpl>() {
            @Mock InputStream loadInputStream(ClassLoader classLoader, String trustStoreLocation) {
                return trustStoreInputStream;
            }

            @Mock String decryptPassword(String password, PasswordEncryptor passwordEncryptor) {
                return "decryptedPassword";
            }

            @Mock KeyStore load(InputStream input, String storepass, String provider, String type) {
                return trustStore;
            }
        };

        // When
        KeyStore result = domainCryptoService.loadTrustStore();

        // Then
        Assert.assertEquals("Should have returned the correct trust store when loading it", trustStore, result);
    }

    @Test
    public void throwsExceptionWhenFailingToLoadTheTrustStore_WSSecurityException(@Injectable InputStream trustStoreInputStream) {
        // Given
        thrown.expect(CryptoException.class);
        thrown.expectMessage("Error loading truststore");

        new MockUp<DomainCryptoServiceImpl>() {
            @Mock InputStream loadInputStream(ClassLoader classLoader, String trustStoreLocation) throws Exception {
                throw new WSSecurityException(WSSecurityException.ErrorCode.SECURITY_ERROR);
            }
        };

        // When
        domainCryptoService.loadTrustStore();
    }

    @Test
    public void throwsExceptionWhenFailingToLoadTheTrustStore_IOException(@Injectable InputStream trustStoreInputStream) {
        // Given
        thrown.expect(CryptoException.class);
        thrown.expectMessage("Error loading truststore");

        new MockUp<DomainCryptoServiceImpl>() {
            @Mock InputStream loadInputStream(ClassLoader classLoader, String trustStoreLocation) throws Exception {
                throw new IOException();
            }
        };

        // When
        domainCryptoService.loadTrustStore();
    }

    @Test
    public void throwsExceptionWhenFailingToLoadTheTrustStoreAndItsLocationIsNull(@Injectable InputStream trustStoreInputStream) {
        // Given
        thrown.expect(CryptoException.class);
        thrown.expectMessage("Could not load truststore, truststore location is empty");

        new Expectations() {{
            domibusPropertyProvider.getResolvedProperty(domain, "domibus.security.truststore.location"); result = null;
        }};

        // When
        domainCryptoService.loadTrustStore();
    }

    @Test
    public void refreshesTheTrustStoreWithTheLoadedTrustStore() {
        // Given
        new MockUp<DomainCryptoServiceImpl>() {
            @Mock KeyStore loadTrustStore() {
                return trustStore;
            }
        };

        // When
        domainCryptoService.refreshTrustStore();

        // Then
        new Verifications() {{
           domainCryptoService.setTrustStore(trustStore);
        }};
    }
}