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

    /*@Test
    public void testGetDefaultDomainNameNonMultitenant() {
        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = false;
        }};

        String defaultDomainName = multiTenancyService.getDefaultDomainName();

        Assert.assertNull(defaultDomainName);
    }

    @Test
    public void testGetDefaultDomainNameMultitenant() {
        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;
        }};

        String defaultDomainName = multiTenancyService.getDefaultDomainName();

        Assert.assertEquals("default", defaultDomainName);
    }*/

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
            domainExtService.getDomain("default");
            result = new DomainDTO("default", "Default");
        }};

        boolean verifyDomainExists = multiTenancyService.verifyDomainExists("default");

        Assert.assertTrue(verifyDomainExists);
    }

    @Test
    public void testVerifyDomainExistsMultitenantException() {
        new Expectations() {{
            domibusConfigurationExtService.isMultiTenantAware();
            result = true;
            domainExtService.getDomain("default");
            result = null;
        }};

        try {
            multiTenancyService.verifyDomainExists("default");
        } catch (FSSetUpException ex) {
            return;
        }
        Assert.fail();
    }
}
