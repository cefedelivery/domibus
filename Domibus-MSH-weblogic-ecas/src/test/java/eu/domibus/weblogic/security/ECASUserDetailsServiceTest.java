package eu.domibus.weblogic.security;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.security.Principal;
import java.util.*;

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

        new Expectations(ecasUserDetailsService) {{
            token.getPrincipal();
            result = username;
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
    public void createUserDetails(@Mocked final Principal principal, @Mocked final UserDetail userDetail) throws Exception {
        final String username = "super";
        final String domainCode = "domain1";

        final Set<Principal> principals = new HashSet<>();
        principals.add(principal);

        new Expectations(ecasUserDetailsService) {{
            domibusPropertyProvider.getProperty(ECASUserDetailsService.ECAS_DOMIBUS_LDAP_GROUP_PREFIX_KEY);
            result = "DIGIT_DOM";

            ecasUserDetailsService.retrieveUserRoleMappings();
            result = userRoleMappings;

            ecasUserDetailsService.retrieveDomainMappings();
            result = domainMappings;

            ecasUserDetailsService.getPrincipals();
            result = principals;

            ecasUserDetailsService.isUserGroupPrincipal((Principal) any);
            result = true;

            principal.getName();
            result = "DIGIT_DOMRADM";

            ecasUserDetailsService.chooseHighestUserGroup((ArrayList<AuthRole>) any);
            result = new SimpleGrantedAuthority(AuthRole.ROLE_ADMIN.name());
        }};

        //tested method
        ecasUserDetailsService.createUserDetails(username);

        new FullVerifications(ecasUserDetailsService) {{
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
        Assert.assertEquals(AuthRole.ROLE_ADMIN, userRoleMappings.get("DIGIT_DOMRADM"));
        Assert.assertEquals(AuthRole.ROLE_AP_ADMIN, userRoleMappings.get("DIGIT_DOMRSADM"));
    }

    @Test
    public void chooseHighestUserGroup() {

        final List<AuthRole> list1 = new ArrayList<>();
        list1.add(AuthRole.ROLE_ADMIN);
        list1.add(AuthRole.ROLE_USER);

        Assert.assertEquals(new SimpleGrantedAuthority(AuthRole.ROLE_ADMIN.name()),
                ecasUserDetailsService.chooseHighestUserGroup(list1));

        list1.add(AuthRole.ROLE_AP_ADMIN);
        Assert.assertEquals(new SimpleGrantedAuthority(AuthRole.ROLE_AP_ADMIN.name()),
                ecasUserDetailsService.chooseHighestUserGroup(list1));
    }

    @Test
    public void setDomainFromEcasGroup_Multitenancy(@Mocked final UserDetail userDetail) {
        final String domainCode = "domain1";
        List<Domain> domains = new ArrayList<>();
        Domain domain1 = new Domain("domain1", "Domain1");
        Domain domain2 = new Domain("domain2", "Domain2");
        domains.add(domain1);
        domains.add(domain2);

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            domainService.getDomains();
            result = domains;
        }};

        //tested method
        ecasUserDetailsService.setDomainFromECASGroup(domainCode, userDetail);

        new Verifications() {{
            String actualDomain;
            userDetail.setDomain(actualDomain = withCapture());
            Assert.assertEquals(domainCode, actualDomain);
        }};
    }

    @Test
    public void setDomainFromEcasGroup_NonMultitenancy(@Mocked final UserDetail userDetail) {
        final String domainCode = DomainService.DEFAULT_DOMAIN.getCode();

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;

        }};

        //tested method
        ecasUserDetailsService.setDomainFromECASGroup(domainCode, userDetail);

        new Verifications() {{
            String actualDomain;
            userDetail.setDomain(actualDomain = withCapture());
            Assert.assertEquals(domainCode, actualDomain);
        }};
    }
}