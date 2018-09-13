package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthType;
import eu.domibus.api.user.UserManagementException;
import eu.domibus.api.user.UserState;
import eu.domibus.common.services.PluginUserService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.csv.CsvCustomColumns;
import eu.domibus.core.csv.CsvExcludedItems;
import eu.domibus.core.csv.CsvService;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.security.AuthenticationEntity;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.PluginUserRO;
import eu.domibus.web.rest.ro.PluginUserResultRO;
import org.apache.cxf.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
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

    @ExceptionHandler({UserManagementException.class})
    public ResponseEntity<ErrorRO> handleUserManagementException(UserManagementException ex) {
        LOG.error(ex.getMessage(), ex);

        ErrorRO error = new ErrorRO(ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONNECTION, "close");
        //keep this for the moment in case the connection close header proves not good in the end
        //headers.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(error.getContentLength()));

        return new ResponseEntity(error, headers, HttpStatus.CONFLICT);
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
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
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
     * @param authType     the authentication type (BASIC or CERTIFICATE)
     * @param authRole     the authorization role
     * @param originalUser the original user
     * @param userName     the user name
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
        final PluginUserResultRO pluginUserROList = findUsers(authType, authRole, originalUser, userName, 0, csvServiceImpl.getMaxNumberRowsToExport());

        try {
            resultText = csvServiceImpl.exportToCSV(pluginUserROList.getEntries(), PluginUserRO.class,
                    CsvCustomColumns.PLUGIN_USER_RESOURCE.getCustomColumns(), CsvExcludedItems.PLUGIN_USER_RESOURCE.getExcludedItems());
        } catch (CsvException e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CsvService.APPLICATION_EXCEL_STR))
                .header("Content-Disposition", "attachment; filename=" + csvServiceImpl.getCsvFilename("pluginusers"))
                .body(resultText);
    }

    /**
     * convert plugin users to PluginUserROs.
     *
     * @return a list of PluginUserROs and the pagination info
     */
    private PluginUserResultRO prepareResponse(List<AuthenticationEntity> users, Long count, int pageStart, int pageSize) {
        List<PluginUserRO> pluginUserROs = domainConverter.convert(users, PluginUserRO.class);
        for (PluginUserRO pluginUserRO : pluginUserROs) {
            pluginUserRO.setStatus(UserState.PERSISTED.name());
            pluginUserRO.setPasswd(null);
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
