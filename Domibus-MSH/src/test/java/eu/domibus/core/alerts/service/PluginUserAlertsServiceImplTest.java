package eu.domibus.core.alerts.service;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.security.UserBase;
import eu.domibus.common.model.security.User;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.security.AuthenticationDAO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class PluginUserAlertsServiceImplTest {

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private AuthenticationDAO userDao;

    @Injectable
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Injectable
    private EventService eventService;

    @Injectable
    MultiDomainAlertConfigurationService alertConfiguration;

    @Tested
    private PluginUserAlertsServiceImpl userAlertsService;

    @Test
    public void testGetMaximumDefaultPasswordAgeProperty() {
        String prop = userAlertsService.getMaximumDefaultPasswordAgeProperty();

        Assert.assertEquals(PluginUserAlertsServiceImpl.MAXIMUM_DEFAULT_PASSWORD_AGE, prop);
    }

    @Test
    public void testGetMaximumPasswordAgeProperty() {
        String prop = userAlertsService.getMaximumPasswordAgeProperty();

        Assert.assertEquals(PluginUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE, prop);
    }

    @Test
    public void testGetAlertTypeForPasswordImminentExpiration() {
        AlertType val = userAlertsService.getAlertTypeForPasswordImminentExpiration();

        Assert.assertEquals(AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION, val);
    }

    @Test
    public void testGetAlertTypeForPasswordExpired() {
        AlertType val = userAlertsService.getAlertTypeForPasswordExpired();

        Assert.assertEquals(AlertType.PLUGIN_PASSWORD_EXPIRED, val);
    }

}
