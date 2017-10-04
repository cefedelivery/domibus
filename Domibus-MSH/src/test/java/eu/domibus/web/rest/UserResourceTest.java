package eu.domibus.web.rest;

import eu.domibus.api.user.User;
import eu.domibus.api.user.UserState;
import eu.domibus.common.services.UserService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.web.rest.ro.UserResponseRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class UserResourceTest {

    @Tested
    UserResource userResource;

    @Injectable
    UserService userService;

    @Injectable
    DomainCoreConverter domainConverter;

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
        userList.add(new User("username", "email", true, new ArrayList<String>(), UserState.PERSISTED));

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
}