package eu.domibus.common.services.impl;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.core.security.AuthenticationDAO;
import eu.domibus.core.security.AuthenticationEntity;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @since 4.0
 */
@RunWith(JMockit.class)
public class PluginUserServiceImplTest {

    @Tested
    private PluginUserServiceImpl pluginUserService;

    @Injectable
    @Qualifier("securityAuthenticationDAO")
    private AuthenticationDAO securityAuthenticationDAO;

    @Injectable
    private BCryptPasswordEncoder bcryptEncoder;

    @Injectable
    private UserRoleDao userRoleDao;

    @Injectable
    private UserDomainService userDomainService;

    @Injectable
    private DomainContextProvider domainProvider;

    @Test(expected = UserManagementException.class)
    public void testUpdateUsersWithDuplicateName() {
        AuthenticationEntity user1 = new AuthenticationEntity();
        user1.setUsername("username1");
        AuthenticationEntity user2 = new AuthenticationEntity();
        user2.setUsername("username1");

        List<AuthenticationEntity> addedUsers = Arrays.asList(new AuthenticationEntity[]{user1, user2});
        List<AuthenticationEntity> updatedUsers = new ArrayList();
        List<AuthenticationEntity> removedUsers = new ArrayList();

        pluginUserService.updateUsers(addedUsers, updatedUsers, removedUsers);
    }

    @Test(expected = UserManagementException.class)
    public void testUpdateUsersWithDuplicateCertificateId() {
        AuthenticationEntity user1 = new AuthenticationEntity();
        user1.setCertificateId("aaa");
        AuthenticationEntity user2 = new AuthenticationEntity();
        user2.setCertificateId("aaa");

        List<AuthenticationEntity> addedUsers = Arrays.asList(new AuthenticationEntity[]{user1, user2});
        List<AuthenticationEntity> updatedUsers = new ArrayList();
        List<AuthenticationEntity> removedUsers = new ArrayList();

        pluginUserService.updateUsers(addedUsers, updatedUsers, removedUsers);
    }
}
