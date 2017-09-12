package eu.domibus.web.rest;


import eu.domibus.api.user.User;
import eu.domibus.api.user.UserRole;
import eu.domibus.api.user.UserState;
import eu.domibus.common.services.UserService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.UserResponseRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/user")
public class UserResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserResource.class);

    private final UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private DomainCoreConverter domainConverter;

    /**
     * {@inheritDoc}
     */
    @RequestMapping(value = {"/users"}, method = RequestMethod.GET)
    public List<UserResponseRO> users() {
        LOG.debug("Retrieving users");
        List<User> users = userService.findUsers();
        return prepareResponse(users);
    }

    @RequestMapping(value = {"/users"}, method = RequestMethod.PUT)
    public void updateUsers(@RequestBody List<UserResponseRO> userROS) {
        updateUserRoles(userROS);
        List<User> users = domainConverter.convert(userROS, User.class);
        userService.updateUsers(users);
    }

    private void updateUserRoles(List<UserResponseRO> userROS) {
        for (UserResponseRO userRo : userROS) {
            if (Objects.equals(userRo.getStatus(), UserState.NEW.name()) || Objects.equals(userRo.getStatus(), UserState.UPDATED.name())) {
                List<String> auths = Arrays.asList(userRo.getRoles().split(","));
                userRo.setAuthorities(auths);
            }
        }
    }


    @RequestMapping(value = {"/save"}, method = RequestMethod.POST)
    public void save(@RequestBody List<UserResponseRO> usersRo) {
        LOG.debug("Saving " + usersRo.size() + "");
        List<eu.domibus.api.user.User> users = domainConverter.convert(usersRo, eu.domibus.api.user.User.class);
        userService.saveUsers(users);
    }

    @RequestMapping(value = {"/userroles"}, method = RequestMethod.GET)
    public List<String> userRoles() {
        List<String> result = new ArrayList<>();
        List<UserRole> userRoles = userService.findUserRoles();
        for (UserRole userRole : userRoles) {
            result.add(userRole.getRole());
        }
        return result;
    }


    /**
     * convert user to userresponsero.
     *
     * @param users
     * @return a list of
     */
    private List<UserResponseRO> prepareResponse(List<User> users) {
        List<UserResponseRO> userResponseROS = domainConverter.convert(users, UserResponseRO.class);
        for (UserResponseRO userResponseRO : userResponseROS) {
            userResponseRO.setStatus("PERSISTED");
            userResponseRO.updateRolesField();
        }
        return userResponseROS;

    }

}
