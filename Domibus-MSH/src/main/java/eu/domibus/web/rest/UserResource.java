package eu.domibus.web.rest;

import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserRole;
import eu.domibus.common.services.UserDetailService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.UserResponseRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/user")
public class UserResource {

    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserResource.class);


    private final UserDetailService userDetailService;

    @Autowired
    public UserResource(UserDetailService userDetailService) {
        this.userDetailService=userDetailService;
    }

    @RequestMapping(value = {"/users"}, method = GET)
    public List<UserResponseRO> users() {
        LOG.info("Retrieving users !");
        List<User> users = userDetailService.findUsers();
        return prepareResponse(users);
    }

    private List<UserResponseRO> prepareResponse(List<User> users) {
        List<UserResponseRO>responses=new ArrayList<>();
        for (User user : users) {
            Collection<UserRole> roles = user.getRoles();
            StringBuilder role= new StringBuilder();
            for (UserRole userRole : roles) {
                role.append(userRole.getName());
            }
            responses.add(new UserResponseRO(user.getUserName(), role.toString(),user.getEmail(),user.isEnabled()));
        }
        return responses;
    }

}
