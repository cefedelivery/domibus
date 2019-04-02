package eu.domibus.configuration.storage;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class StorageProviderImplTest {

    @Injectable
    protected StorageFactory storageFactory;

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    Map<Domain, Storage> instances = new HashMap<>();

    @Tested
    StorageProviderImpl storageProvider;

    @Test
    public void init(@Injectable Storage storage) {
        List<Domain> domains = new ArrayList<>();
        final Domain domain = DomainService.DEFAULT_DOMAIN;
        domains.add(domain);

        new Expectations() {{
            domainService.getDomains();
            result = domains;

            storageFactory.create(domain);
            result = storage;
        }};

        storageProvider.init();

        new Verifications() {{
            storageFactory.create(domain);
            times = 1;

            instances.put(domain, storage);
        }};
    }

    @Test
    public void forDomain() {
        final Domain domain = DomainService.DEFAULT_DOMAIN;

        storageProvider.forDomain(domain);

        new Verifications() {{
            instances.get(domain);
        }};
    }

    @Test
    public void getCurrentStorage(@Injectable Storage storage) {
        final Domain domain = DomainService.DEFAULT_DOMAIN;

        new Expectations(storageProvider) {{
            domainContextProvider.getCurrentDomainSafely();
            result = domain;

            storageProvider.forDomain(domain);
            result = storage;
        }};

        final Storage currentStorage = storageProvider.getCurrentStorage();
        Assert.assertEquals(currentStorage, storage);
    }

    @Test
    public void savePayloadsInDatabase(@Injectable Storage storage) {
        new Expectations(storageProvider) {{
            storageProvider.getCurrentStorage();
            result = storage;

            storage.getStorageDirectory();
            result = null;
        }};

        Assert.assertTrue(storageProvider.savePayloadsInDatabase());
    }

    @Test
    public void testSavePayloadsInDatabaseWithFileSystemStorage(@Injectable Storage storage,
                                                                @Injectable File file) {
        new Expectations(storageProvider) {{
            storageProvider.getCurrentStorage();
            result = storage;

            storage.getStorageDirectory();
            result = file;

            file.getName();
            result = "/home/storage";
        }};

        Assert.assertFalse(storageProvider.savePayloadsInDatabase());
    }
}