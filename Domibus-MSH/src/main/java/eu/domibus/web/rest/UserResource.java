package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.user.User;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserRole;
import eu.domibus.api.user.UserState;
import eu.domibus.common.services.CsvService;
import eu.domibus.common.services.UserService;
import eu.domibus.common.services.impl.CsvServiceImpl;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.UserResponseRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/user")
public class UserResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserResource.class);

    @Autowired
    @Lazy
    @Qualifier("superUserManagementService")
    private UserService superUserManagementService;

    @Autowired
    @Lazy
    @Qualifier("userManagementService")
    private UserService userManagementService;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private CsvServiceImpl csvServiceImpl;

    @Autowired
    private AuthUtils authUtils;

    private UserService getUserService() {
        if (authUtils.isSuperAdmin()) {
            return superUserManagementService;
        } else {
            return userManagementService;
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserManagementException.class})
    public ErrorRO handleUserManagementException(Exception ex) {
        return new ErrorRO(ex.getMessage());
    }

    /**
     * {@inheritDoc}
     */
    @RequestMapping(value = {"/users"}, method = RequestMethod.GET)
    public List<UserResponseRO> users() {
        LOG.debug("Retrieving users");

        List<User> users = getUserService().findUsers();

        return prepareResponse(users);
    }

    @RequestMapping(value = {"/users"}, method = RequestMethod.PUT)
    public void updateUsers(@RequestBody List<UserResponseRO> userROS) {
        LOG.debug("Update Users was called: " + userROS);
        updateUserRoles(userROS);
        List<User> users = domainConverter.convert(userROS, User.class);
        getUserService().updateUsers(users);
    }

    private void updateUserRoles(List<UserResponseRO> userROS) {
        for (UserResponseRO userRo : userROS) {
            if (Objects.equals(userRo.getStatus(), UserState.NEW.name()) || Objects.equals(userRo.getStatus(), UserState.UPDATED.name())) {
                List<String> auths = Arrays.asList(userRo.getRoles().split(","));
                userRo.setAuthorities(auths);
            }
        }
    }

    @RequestMapping(value = {"/userroles"}, method = RequestMethod.GET)
    public List<String> userRoles() {
        List<String> result = new ArrayList<>();
        List<UserRole> userRoles = getUserService().findUserRoles();
        for (UserRole userRole : userRoles) {
            result.add(userRole.getRole());
        }

        // ROLE_AP_ADMIN role is available only to superusers
        if (authUtils.isSuperAdmin()) {
            result.add(AuthRole.ROLE_AP_ADMIN.name());
        }

        return result;
    }

    /**
     * This method returns a CSV file with the contents of User table
     *
     * @return CSV file with the contents of User table
     */
    @RequestMapping(path = "/csv", method = RequestMethod.GET)
    public ResponseEntity<String> getCsv() {
        String resultText;

        // get list of users
        final List<UserResponseRO> userResponseROList = users();

        // excluding unneeded columns
        csvServiceImpl.setExcludedItems(CsvExcludedItems.USER_RESOURCE.getExcludedItems());

        // needed for empty csv file purposes
        csvServiceImpl.setClass(UserResponseRO.class);

        // column customization
        csvServiceImpl.customizeColumn(CsvCustomColumns.USER_RESOURCE.getCustomColumns());

        try {
            resultText = csvServiceImpl.exportToCSV(userResponseROList);
        } catch (CsvException e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CsvService.APPLICATION_EXCEL_STR))
                .header("Content-Disposition", "attachment; filename=" + csvServiceImpl.getCsvFilename("users"))
                .body(resultText);
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
