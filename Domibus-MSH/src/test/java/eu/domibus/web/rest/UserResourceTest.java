package eu.domibus.web.rest;

import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.user.User;
import eu.domibus.api.user.UserState;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.services.UserService;
import eu.domibus.common.services.impl.CsvServiceImpl;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.web.rest.ro.UserResponseRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class UserResourceTest {

    @Tested
    UserResource userResource;

    @Injectable
    Map<String, UserService> userServices;

    @Injectable
    @Qualifier("userManagementService")
    UserService userService;

    @Injectable
    DomainCoreConverter domainConverter;

    @Injectable
    private CsvServiceImpl csvServiceImpl;

    @Injectable
    private AuthUtils authUtils;

    private List<UserResponseRO> getUserResponseList() {
        final List<UserResponseRO> userResponseROList = new ArrayList<>();
        UserResponseRO userResponseRO = getUserResponseRO();
        userResponseROList.add(userResponseRO);
        return userResponseROList;
    }

    private UserResponseRO getUserResponseRO() {
        UserResponseRO userResponseRO = new UserResponseRO();
        userResponseRO.setUserName("username");
        userResponseRO.setEmail("email");
        userResponseRO.setActive(true);
        userResponseRO.setAuthorities(new ArrayList<String>());
        userResponseRO.setStatus("PERSISTED");
        return userResponseRO;
    }

    @Test
    public void testUsers() {
        // Given
        final List<User> userList = new ArrayList<User>();
        userList.add(new User("username", "email", true, new ArrayList<String>(), UserState.PERSISTED, null));

        final List<UserResponseRO> userResponseROList = getUserResponseList();

        new Expectations() {{
            userService.findUsers();
            result = userList;

            domainConverter.convert(userList, UserResponseRO.class);
            result = userResponseROList;
        }};

        // When
        List<UserResponseRO> userResponseROS = userResource.users();

        // Then
        Assert.assertNotNull(userResponseROS);
        UserResponseRO userResponseRO = getUserResponseRO();
        Assert.assertEquals(userResponseRO, userResponseROS.get(0));
    }

    @Test
    public void testGetCsv() throws EbMS3Exception {
        // Given
        List<UserResponseRO> usersResponseROList = new ArrayList<>();
        UserResponseRO userResponseRO = new UserResponseRO("user1", "email@email.com", true);
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_ADMIN");
        userResponseRO.setAuthorities(roles);
        usersResponseROList.add(userResponseRO);
        new Expectations(userResource) {{
           userResource.users();
           result = usersResponseROList;
           csvServiceImpl.exportToCSV(usersResponseROList);
           result = "Username, Email, Active, Roles" + System.lineSeparator() +
                   "user1, email@email.com, true, ROLE_ADMIN" + System.lineSeparator();
        }};

        // When
        final ResponseEntity<String> csv = userResource.getCsv();

        // Then
        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
        Assert.assertEquals("Username, Email, Active, Roles" + System.lineSeparator() +
                "user1, email@email.com, true, ROLE_ADMIN" + System.lineSeparator(), csv.getBody());
    }
}