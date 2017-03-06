package eu.domibus.web.rest;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.web.security.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/api")
public class AuthenticationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);

    @Autowired
    private AuthenticationService authenticationService;

    @RequestMapping(value = "authenticate", method = RequestMethod.POST)
    public UserDTO authenticate(@RequestBody LoginDTO loginDTO, HttpServletResponse response) throws Exception {
        LOGGER.info("Authenticating using [{}]", loginDTO);
        return authenticationService.authenticate(loginDTO);
    }

    @RequestMapping(value = "user", method = RequestMethod.GET)
    public String getUser() {
        User securityUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return securityUser.getUsername();
    }

    @RequestMapping(value = "testPost", method = RequestMethod.POST)
    public UserDTO testPost(@RequestBody LoginDTO loginDTO, HttpServletRequest request) throws Exception {
        LOGGER.info("-------------Testing post", loginDTO);
        final UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testPost");
        return userDTO;
    }

    @RequestMapping(value = "testGet", method = RequestMethod.GET)
    public UserDTO testGet() throws Exception {
        LOGGER.info("-------------Testing get");
        final UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testGet");
        return userDTO;
    }

    @RequestMapping(value = "domibusinfo", method = RequestMethod.GET)
    public DomibusInfoDTO getDomibusInfo() throws Exception {
        LOGGER.info("-------------domibus info");
        final DomibusInfoDTO domibusInfoDTO = new DomibusInfoDTO();
        domibusInfoDTO.setVersion("4.0");
        return domibusInfoDTO;
    }

    @RequestMapping(value = "errorlog", method = RequestMethod.GET)
    public ErrorLogResultRO getErrorLog() throws Exception {
        LOGGER.info("-------------errorLog");
        ErrorLogResultRO result = new ErrorLogResultRO();

        List<ErrorLogRO> errorLogEntries = new ArrayList<>();
        for (int i=0; i < 100; i++) {
            ErrorLogRO entry1 = new ErrorLogRO();
            entry1.setErrorCode("DOM_00" + i);
            entry1.setErrorDetail("Error occurred while calling the backend " + i);
            entry1.setErrorSignalMessageId("signalid " + i);
            entry1.setMessageInErrorId("messageIn error" + i);
            entry1.setMshRole("SENDING");
            entry1.setNotified(new Timestamp(System.currentTimeMillis()));
            entry1.setTimestamp(new Timestamp(System.currentTimeMillis()));
            errorLogEntries.add(entry1);
        }

        result.setErrorLogEntries(errorLogEntries);
        result.setCount(25);
        result.setErrorCodes(ErrorCode.values());
        result.setMshRoles(MSHRole.values());
        return result;
    }

    @RequestMapping(value = "logout", method = RequestMethod.GET)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            LOGGER.info("Logging out user [" + auth.getName() + "]");
        }

        new CookieClearingLogoutHandler("JSESSIONID", "XSRF-TOKEN").logout(request, response, null);
        LOGGER.debug("Cleared cookies");
        new SecurityContextLogoutHandler().logout(request, response, auth);
        LOGGER.debug("Logged out");
    }
}
