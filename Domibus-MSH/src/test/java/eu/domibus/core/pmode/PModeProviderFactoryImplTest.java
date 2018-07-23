package eu.domibus.core.pmode;

import eu.domibus.api.multitenancy.Domain;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class PModeProviderFactoryImplTest {

    @Injectable
    ApplicationContext applicationContext;

    @Tested
    PModeProviderFactoryImpl pModeProviderFactory;

    @Test
    public void testCreateDomainPModeProvider(@Injectable Domain domain) {
        pModeProviderFactory.createDomainPModeProvider(domain);

        new Verifications() {{
            applicationContext.getBean(DynamicDiscoveryPModeProvider.class, domain);
        }};
    }
}
