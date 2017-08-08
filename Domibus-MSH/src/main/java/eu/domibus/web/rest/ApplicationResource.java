package eu.domibus.web.rest;

import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.web.rest.ro.DomibusInfoRO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/application")
public class ApplicationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationResource.class);

    @Autowired
    private DomibusPropertiesService domibusPropertiesService;

    @RequestMapping(value = "info", method = RequestMethod.GET)
    public DomibusInfoRO getDomibusInfo() {
        LOG.debug("Getting application info");
        final DomibusInfoRO domibusInfoRO = new DomibusInfoRO();
        domibusInfoRO.setVersion(domibusPropertiesService.getDisplayVersion());
        return domibusInfoRO;
    }
}
