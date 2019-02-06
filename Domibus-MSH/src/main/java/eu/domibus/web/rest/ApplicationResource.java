package eu.domibus.web.rest;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.rest.ro.DomibusInfoRO;
import eu.domibus.web.rest.ro.PasswordPolicyRO;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Cosmin Baciu, Catalin Enache
 * @since 3.3
 * <p>
 * Rest for getting application related information
 */
@RestController
@RequestMapping(value = "/rest/application")
public class ApplicationResource {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(ApplicationResource.class);

    String PASSWORD_POLICY_PATTERN = "domibus.passwordPolicy.pattern";
    String PASSWORD_POLICY_VALIDATION_MESSAGE = "domibus.passwordPolicy.validationMessage";
    String PLUGIN_PASSWORD_POLICY_PATTERN = "domibus.plugin.passwordPolicy.pattern";
    String PLUGIN_PASSWORD_POLICY_VALIDATION_MESSAGE = "domibus.plugin.passwordPolicy.validationMessage";

    protected static final String DOMIBUS_CUSTOM_NAME = "domibus.UI.title.name";

    @Autowired
    private DomibusPropertiesService domibusPropertiesService;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    @Autowired
    protected DomainService domainService;

    @Autowired
    protected DomainCoreConverter domainCoreConverter;

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected AuthUtils authUtils;

    /**
     * Rest method for the Domibus Info (Version, Build Time, ...)
     *
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
     *
     * @return Domibus Customized Name
     */
    @RequestMapping(value = "name", method = RequestMethod.GET)
    public String getDomibusName() {
        LOG.debug("Getting application name");
        Domain domain = null;
        // We check this because, for non-authenticated users, the domain would sometimes be recycled from some other thread from the pool and it would have a random domain.
        if (authUtils.getAuthenticatedUser() != null) {
            domain = domainContextProvider.getCurrentDomainSafely();
        }
        if (domain == null) {
            domain = DomainService.DEFAULT_DOMAIN;
        }
        return domibusPropertyProvider.getDomainProperty(domain, DOMIBUS_CUSTOM_NAME);
    }

    /**
     * Rest get method for multi-tenancy status
     *
     * @return true if multi-tenancy is enabled
     */
    @RequestMapping(value = "multitenancy", method = RequestMethod.GET)
    public Boolean getMultiTenancy() {
        LOG.debug("Getting multi-tenancy status");
        return domibusConfigurationService.isMultiTenantAware();
    }

    /**
     * Retrieve all configured domains in multi-tenancy mode
     *
     * @return a list of domains
     */
    @RequestMapping(value = "domains", method = RequestMethod.GET)
    public List<DomainRO> getDomains() {
        LOG.debug("Getting domains");
        return domainCoreConverter.convert(domainService.getDomains(), DomainRO.class);
    }

    @RequestMapping(value = "fourcornerenabled", method = RequestMethod.GET)
    public boolean getFourCornerModelEnabled() {
        LOG.debug("Getting four corner enabled");
        return domibusConfigurationService.isFourCornerEnabled();
    }

    /**
     * Returns true is an external Authentication Provider (e.g. ECAS) is enabled
     *
     * @return
     */
    @RequestMapping(value = "extauthproviderenabled", method = RequestMethod.GET)
    public boolean getExtAuthProviderEnabled() {
        LOG.debug("Getting external authentication provider enabled");
        return domibusConfigurationService.isExtAuthProviderEnabled();
    }

    /**
     * Retrieves the password policy info
     *
     * @return password policy info
     */
    @RequestMapping(value = "passwordPolicy", method = RequestMethod.GET)
    public PasswordPolicyRO getPasswordPolicy() {
        LOG.debug("Getting password policy");

        String pattern = this.getPasswordPattern();
        String validationMessage = this.getPasswordValidationMessage();

        return new PasswordPolicyRO(pattern, validationMessage);
    }

    /**
     * Retrieves the password policy info for plugin users
     *
     * @return password policy info
     */
    @RequestMapping(value = "pluginPasswordPolicy", method = RequestMethod.GET)
    public PasswordPolicyRO getPluginUsersPasswordPolicy() {
        LOG.debug("Getting plugin password policy");

        String pattern = this.getPluginPasswordPattern();
        String validationMessage = this.getPluginPasswordValidationMessage();

        return new PasswordPolicyRO(pattern, validationMessage);
    }

    private String getPasswordPattern() {
        return domibusPropertyProvider.getDomainProperty(PASSWORD_POLICY_PATTERN);
    }

    private String getPasswordValidationMessage() {
        return domibusPropertyProvider.getDomainProperty(PASSWORD_POLICY_VALIDATION_MESSAGE);
    }

    private String getPluginPasswordPattern() {
        return domibusPropertyProvider.getDomainProperty(PLUGIN_PASSWORD_POLICY_PATTERN);
    }
    private String getPluginPasswordValidationMessage() {
        return domibusPropertyProvider.getDomainProperty(PLUGIN_PASSWORD_POLICY_VALIDATION_MESSAGE);
    }
}
