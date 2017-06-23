package eu.domibus.web.rest;


import eu.domibus.api.user.User;
import eu.domibus.common.services.UserService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.UserResponseRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/user")
public class UserResource {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserResource.class);

    private final UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private DomainCoreConverter domainConverter;

    @RequestMapping(value = {"/users"}, method = GET)
    public List<UserResponseRO> users() {
        LOG.info("Retrieving usersRo !");
        List<User> users = userService.findUsers();
        return prepareResponse(users);
    }

    @RequestMapping(value = {"/save"}, method = POST)
    public void save(@RequestBody List<UserResponseRO> usersRo) {
        LOG.info("Saving "+ usersRo.size()+"");
        List<eu.domibus.api.user.User> users = domainConverter.convert(usersRo, eu.domibus.api.user.User.class);
        userService.saveUsers(users);
    }


    private List<UserResponseRO> prepareResponse(List<User> users) {
        List<UserResponseRO> userResponseROS = domainConverter.convert(users, UserResponseRO.class);
        for (UserResponseRO userResponseRO : userResponseROS) {
            userResponseRO.setStatus("PERSISTED");
            userResponseRO.updateRolesField();
        }
        return  userResponseROS;

    }

}
