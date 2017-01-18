package eu.domibus.pki;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Arun Raj
 * @since 3.3
 */
@RunWith(JMockit.class)
public class PolicyServiceImplTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PolicyServiceImplTest.class);
    private static final String TEST_RESOURCES_DIR = "./src/test/resources";
    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Tested
    PolicyServiceImpl policyService;

    @Test
    public void testIsNoSecurityPolicy_NullPolicy() {
        //when null policy is specified
        boolean result1 = policyService.isNoSecurityPolicy(null);
        Assert.assertTrue("Expected NoSecurityPolicy as true when null input provided", result1 == true);
    }

    @Test
    public void testIsNoSecurityPolicy_DoNothingPolicy() {
        //when doNothingPolicy.xml is specified
        new Expectations() {{
            domibusConfigurationService.getConfigLocation();
            result = TEST_RESOURCES_DIR;
        }};

        Policy doNothingPolicy = policyService.parsePolicy("policies/doNothingPolicy.xml");
        boolean result2 = policyService.isNoSecurityPolicy(doNothingPolicy);
        Assert.assertTrue(result2 == true);
    }

    @Test
    public void testIsNoSecurityPolicy_SignOnPolicy() {
        new Expectations() {{
            domibusConfigurationService.getConfigLocation();
            result = TEST_RESOURCES_DIR;
        }};

        Policy signOnlyPolicy = policyService.parsePolicy("policies/signOnly.xml");
        boolean result3 = policyService.isNoSecurityPolicy(signOnlyPolicy);
        Assert.assertTrue(result3 == false);
    }

    @Test
    public void testParsePolicyException() {

        try {
            policyService.parsePolicy("NonExistentFileLocation");
        } catch (Exception e) {
            Assert.assertTrue("Expecting ConfigurationException", e instanceof ConfigurationException);
        }
    }

}
