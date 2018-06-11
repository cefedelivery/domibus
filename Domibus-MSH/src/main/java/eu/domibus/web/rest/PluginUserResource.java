package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthType;
import eu.domibus.api.user.User;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.common.services.CsvService;
import eu.domibus.common.services.PluginUserService;
import eu.domibus.common.services.impl.CsvServiceImpl;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.security.AuthenticationEntity;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.PluginUserRO;
import eu.domibus.web.rest.ro.PluginUserResultRO;
import eu.domibus.web.rest.ro.UserResponseRO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.cxf.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Pion
 * @since 4.0
 */
@RestController
@RequestMapping(value = "/rest/plugin")
public class PluginUserResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserResource.class);

    @Autowired
    private PluginUserService pluginUserService;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private CsvServiceImpl csvServiceImpl;

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserManagementException.class})
    public ErrorRO handleUserManagementException(Exception ex) {
        return new ErrorRO(ex.getMessage());
    }

    @RequestMapping(value = {"/users"}, method = RequestMethod.GET)
    public PluginUserResultRO findUsers(
            @RequestParam(value = "authType", required = false) AuthType authType,
            @RequestParam(value = "authRole", required = false) AuthRole authRole,
            @RequestParam(value = "originalUser", required = false) String originalUser,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "pageStart", defaultValue = "0") int pageStart,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        LOG.debug("Retrieving plugin users");

        Long count = pluginUserService.countUsers(authType, authRole, originalUser, userName);

        List<AuthenticationEntity> users;
        if (count > 0) {
            users = pluginUserService.findUsers(authType, authRole, originalUser, userName, pageStart, pageSize);
        } else {
            users = new ArrayList<>();
        }

        return prepareResponse(users, count, pageStart, pageSize);
    }

    @RequestMapping(value = {"/users"}, method = RequestMethod.PUT)
    public void updateUsers(@RequestBody List<PluginUserRO> userROs) {
        LOG.debug("Update plugin users was called: " + userROs);

        List<PluginUserRO> addedUsersRO = userROs.stream().filter(u -> UserState.NEW.name().equals(u.getStatus())).collect(Collectors.toList());
        List<PluginUserRO> updatedUsersRO = userROs.stream().filter(u -> UserState.UPDATED.name().equals(u.getStatus())).collect(Collectors.toList());
        List<PluginUserRO> removedUsersRO = userROs.stream().filter(u -> UserState.REMOVED.name().equals(u.getStatus())).collect(Collectors.toList());

        List<AuthenticationEntity> addedUsers = domainConverter.convert(addedUsersRO, AuthenticationEntity.class);
        List<AuthenticationEntity> updatedUsers = domainConverter.convert(updatedUsersRO, AuthenticationEntity.class);
        List<AuthenticationEntity> removedUsers = domainConverter.convert(removedUsersRO, AuthenticationEntity.class);
        pluginUserService.updateUsers(addedUsers, updatedUsers, removedUsers);
    }

    /**
     * This method returns a CSV file with the contents of Plugin User table
     *
     * @return CSV file with the contents of Plugin User table
     */
    @RequestMapping(path = "/csv", method = RequestMethod.GET)
    public ResponseEntity<String> getCsv(
            @RequestParam(value = "authType", required = false) AuthType authType,
            @RequestParam(value = "authRole", required = false) AuthRole authRole,
            @RequestParam(value = "originalUser", required = false) String originalUser,
            @RequestParam(value = "userName", required = false) String userName) {
        String resultText;

        // get list of users
        final PluginUserResultRO pluginUserROList = findUsers(authType, authRole, originalUser, userName, 0, CsvService.MAX_NUMBER_OF_ENTRIES);

        // excluding unneeded columns
        csvServiceImpl.setExcludedItems(CsvExcludedItems.USER_RESOURCE.getExcludedItems());

        // needed for empty csv file purposes
        csvServiceImpl.setClass(PluginUserRO.class);

        // column customization
        csvServiceImpl.customizeColumn(CsvCustomColumns.USER_RESOURCE.getCustomColumns());

        try {
            resultText = csvServiceImpl.exportToCSV(pluginUserROList.getEntries());
        } catch (CsvException e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CsvService.APPLICATION_EXCEL_STR))
                .header("Content-Disposition", "attachment; filename=" + csvServiceImpl.getCsvFilename("plugin-users"))
                .body(resultText);
    }

    /**
     * convert plugin users to PluginUserROs.
     *
     * @param users
     * @return a list of
     */
    private PluginUserResultRO prepareResponse(List<AuthenticationEntity> users, Long count, int pageStart, int pageSize) {
        List<PluginUserRO> pluginUserROs = domainConverter.convert(users, PluginUserRO.class);
        for (PluginUserRO pluginUserRO : pluginUserROs) {
            pluginUserRO.setStatus(UserState.PERSISTED.name());
            if (StringUtils.isEmpty(pluginUserRO.getCertificateId())) {
                pluginUserRO.setAuthenticationType(AuthType.BASIC.name());
            } else {
                pluginUserRO.setAuthenticationType(AuthType.CERTIFICATE.name());
            }
        }

        PluginUserResultRO result = new PluginUserResultRO();

        result.setEntries(pluginUserROs);
        result.setCount(count);
        result.setPage(pageStart);
        result.setPageSize(pageSize);

        return result;
    }
}
