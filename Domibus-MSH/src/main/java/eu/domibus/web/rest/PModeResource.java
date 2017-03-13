package eu.domibus.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/pmode")
public class PModeResource {

    private static final Logger LOG = LoggerFactory.getLogger(PModeResource.class);

    @RequestMapping(value = "test", method = RequestMethod.GET)
    public String getUser() {
        return "pmodetest";
    }
}
