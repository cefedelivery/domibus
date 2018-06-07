package eu.domibus.web.rest;

import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.web.rest.ro.DomibusInfoRO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/application")
public class ApplicationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationResource.class);
    static final String FOURCORNERMODEL_ENABLED_KEY = "domibus.fourcornermodel.enabled";

    @Autowired
    private DomibusPropertiesService domibusPropertiesService;

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @RequestMapping(value = "info", method = RequestMethod.GET)
    public DomibusInfoRO getDomibusInfo() {
        LOG.debug("Getting application info");
        final DomibusInfoRO domibusInfoRO = new DomibusInfoRO();
        domibusInfoRO.setVersion(domibusPropertiesService.getDisplayVersion());
        return domibusInfoRO;
    }

    @RequestMapping(value = "fourcornerenabled", method = RequestMethod.GET)
    public boolean getFourCornerModelEnabled() {
        LOG.debug("Getting four corner enabled");
        return Boolean.parseBoolean(domibusProperties.getProperty(FOURCORNERMODEL_ENABLED_KEY, "true"));
    }
}
