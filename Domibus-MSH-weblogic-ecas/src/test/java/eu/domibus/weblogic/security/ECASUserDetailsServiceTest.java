package eu.domibus.weblogic.security;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.common.model.security.UserDetail;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class ECASUserDetailsServiceTest {

    @Tested
    ECASUserDetailsService ecasUserDetailsService;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    private Map<String, AuthRole> userRoleMappings = new HashMap<>();
    private Map<String, String> domainMappings = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        userRoleMappings.put("DIGIT_DOMRADM", AuthRole.ROLE_ADMIN);
        domainMappings.put("DIGIT_DOMDDOMN1", "domain1");
    }

    @Test
    public void loadUserDetails(@Mocked final PreAuthenticatedAuthenticationToken token, @Mocked final UserDetail userDetail) {
        final String username = "super";

//        UserDetail userDetail = new UserDetail(null);

        new Expectations(ecasUserDetailsService) {{
//            ecasUserDetailsService.isWeblogicSecurity();
//            result = true;
//
//            ecasUserDetailsService.retrieveUserRoleMappings();
//            result = userRoleMappings;
//
//            ecasUserDetailsService.retrieveDomainMappings();
//            result = domainMappings;
//
//            ecasUserDetailsService.getPrincipals();
            token.getPrincipal();
            result = username;
//
             ecasUserDetailsService.loadUserByUsername(username);
             result = userDetail;

        }};

        //tested method
        final UserDetails userDetails = ecasUserDetailsService.loadUserDetails(token);
        Assert.assertNotNull(userDetails);
    }

    @Test
    public void loadUserByUsername(@Mocked final UserDetail userDetail) throws Exception {
        final String username = "super";

        new Expectations(ecasUserDetailsService) {{
            ecasUserDetailsService.isWeblogicSecurity();
            result = true;

            ecasUserDetailsService.createUserDetails(username);
            result = userDetail;
        }};

        //tested method
        ecasUserDetailsService.loadUserByUsername(username);

        new FullVerifications() {{
            String actualUsername;
            ecasUserDetailsService.createUserDetails(actualUsername = withCapture());
            times = 1;
            Assert.assertEquals(username, actualUsername);
        }};
    }


    @Test
    public void retrieveDomainMappings() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(ECASUserDetailsService.ECAS_DOMIBUS_DOMAIN_MAPPINGS_KEY);
            result = "DIGIT_DOMDDOMN1=domain1;";
        }};

        //tested method
        Map<String, String> domainMappings = ecasUserDetailsService.retrieveDomainMappings();

        Assert.assertTrue(domainMappings.containsKey("DIGIT_DOMDDOMN1"));
        Assert.assertEquals("domain1", domainMappings.get("DIGIT_DOMDDOMN1"));
    }

    @Test
    public void retrieveUserRoleMappings() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(ECASUserDetailsService.ECAS_DOMIBUS_USER_ROLE_MAPPINGS_KEY);
            result = "DIGIT_DOMRUSR=ROLE_USER;DIGIT_DOMRADM=ROLE_ADMIN;DIGIT_DOMRSADM=ROLE_AP_ADMIN;";
        }};

        //tested method
        Map<String, AuthRole> userRoleMappings = ecasUserDetailsService.retrieveUserRoleMappings();

        Assert.assertTrue(userRoleMappings.entrySet().size() == 3);
        Assert.assertEquals(AuthRole.ROLE_USER, userRoleMappings.get("DIGIT_DOMRUSR"));
    }
}