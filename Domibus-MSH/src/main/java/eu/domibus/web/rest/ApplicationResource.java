package eu.domibus.web.rest;

import eu.domibus.api.property.DomibusPropertyProvider;
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
 * <p>
 * Rest for getting application related information
 */
@RestController
@RequestMapping(value = "/rest/application")
public class ApplicationResource {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationResource.class);
    protected static final String DOMIBUS_CUSTOM_NAME = "domibus.UI.title.name";
    protected static final String DOMIBUS_DEFAULTVALUE_NAME = "Domibus";

    @Autowired
    private DomibusPropertiesService domibusPropertiesService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    /**
     * Rest method for the Domibus Info (Version, Build Time, ...)
     * @return Domibus Info
     */
    @RequestMapping(value = "info", method = RequestMethod.GET)
    public DomibusInfoRO getDomibusInfo() {
        LOG.debug("Getting application info");
        final DomibusInfoRO domibusInfoRO = new DomibusInfoRO();
        domibusInfoRO.setVersion(domibusPropertiesService.getDisplayVersion());
        return domibusInfoRO;
    }

    /**
     * Rest get method for the Domibus Customized Name
     * @return Domibus Customized Name (
     */
    @RequestMapping(value = "name", method = RequestMethod.GET)
    public String getDomibusName() {
        LOG.debug("Getting application name");
        return domibusPropertyProvider.getProperty(DOMIBUS_CUSTOM_NAME, DOMIBUS_DEFAULTVALUE_NAME);
    }
}
