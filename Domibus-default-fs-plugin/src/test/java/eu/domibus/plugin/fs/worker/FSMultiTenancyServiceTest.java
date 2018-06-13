package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static eu.domibus.plugin.fs.worker.FSSendMessagesService.DEFAULT_DOMAIN;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@RunWith(JMockit.class)
public class FSMultiTenancyServiceTest {

    @Tested
    private FSMultiTenancyService multiTenancyService;

    @Injectable
    private DomibusConfigurationExtService domibusConfigurationExtService;

    @Injectable
    private DomainExtService domainExtService;

    @Test
    public void testVerifyDomainExistsNonMultitenant() {
        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = false;
        }};

        boolean verifyDomainExists = multiTenancyService.verifyDomainExists("domain");

        Assert.assertFalse(verifyDomainExists);
    }

    @Test
    public void testVerifyDomainExistsMultitenantOk() {
        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;
            domainExtService.getDomain(DEFAULT_DOMAIN);
            result = new DomainDTO(DEFAULT_DOMAIN, "Default");
        }};

        boolean verifyDomainExists = multiTenancyService.verifyDomainExists(DEFAULT_DOMAIN);

        Assert.assertTrue(verifyDomainExists);
    }

    @Test
    public void testVerifyDomainExistsMultitenantException() {
        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;
            domainExtService.getDomain(DEFAULT_DOMAIN);
            result = null;
        }};

        try {
            multiTenancyService.verifyDomainExists(DEFAULT_DOMAIN);
        } catch (FSSetUpException ex) {
            return;
        }
        Assert.fail();
    }
}
