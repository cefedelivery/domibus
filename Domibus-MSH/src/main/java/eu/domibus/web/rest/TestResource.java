package eu.domibus.web.rest;

import eu.domibus.web.rest.ro.LoginRO;
import eu.domibus.web.rest.ro.UserRO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/test")
public class TestResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestResource.class);

    @RequestMapping(value = "testPost", method = RequestMethod.POST)
    public UserRO testPost(@RequestBody LoginRO loginRO, HttpServletRequest request) {
        LOGGER.info("-------------Testing post", loginRO);
        final UserRO userRO = new UserRO();
        userRO.setUsername("testPost");
        return userRO;
    }

    @RequestMapping(value = "testGet", method = RequestMethod.GET)
    public UserRO testGet() {
        LOGGER.info("-------------Testing get");
        final UserRO userRO = new UserRO();
        userRO.setUsername("testGet");
        return userRO;
    }





}
