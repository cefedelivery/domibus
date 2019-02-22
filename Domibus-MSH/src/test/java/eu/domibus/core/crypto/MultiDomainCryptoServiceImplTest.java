package eu.domibus.core.crypto;

import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.core.crypto.api.DomainCryptoServiceFactory;
import mockit.Injectable;
import mockit.Tested;
import org.junit.Assert;
import org.junit.Test;

import java.security.InvalidParameterException;

import static org.junit.Assert.assertEquals;

public class MultiDomainCryptoServiceImplTest {

    @Tested
    MultiDomainCryptoServiceImpl cryptoService;

    @Injectable
    DomainCryptoServiceFactory domainCertificateProviderFactory;

    @Injectable
    private DomibusCacheService domibusCacheService;

    @Test
    public void checkTruststoreTypeValidation() {

        // happy flow :
        cryptoService.validateTruststoreType("jks", "test.jks");
        cryptoService.validateTruststoreType("jks", "test.JKS");
        cryptoService.validateTruststoreType("pkcs12", "test_filename.pfx");
        cryptoService.validateTruststoreType("pkcs12", "test_filename.p12");

        // negative flow :
        try {
            cryptoService.validateTruststoreType("jks", "test_filename_wrong_extension.p12");
            Assert.fail("Expected exception was not raised!");
        } catch (InvalidParameterException e) {
            assertEquals(true, e.getMessage().contains("jks"));
        }

        try {
            cryptoService.validateTruststoreType("jks", "test_filename_no_extension");
            Assert.fail("Expected exception was not raised!");
        } catch (InvalidParameterException e) {
            assertEquals(true, e.getMessage().contains("jks"));
        }

        try {
            cryptoService.validateTruststoreType("pkcs12", "test_filename_unknown_extension.txt");
            Assert.fail("Expected exception was not raised!");
        } catch (InvalidParameterException e) {
            assertEquals(true, e.getMessage().contains("pkcs12"));
        }
    }
}