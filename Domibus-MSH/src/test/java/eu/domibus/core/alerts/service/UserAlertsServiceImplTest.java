package eu.domibus.core.alerts.service;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.converters.UserConverter;
import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.dao.security.UserDaoBase;
import eu.domibus.common.dao.security.UserPasswordHistoryDao;
import eu.domibus.common.dao.security.UserRoleDao;
import eu.domibus.common.model.security.UserEntityBase;
import eu.domibus.common.model.security.User;
import eu.domibus.common.model.security.UserLoginErrorReason;
import eu.domibus.common.services.UserPersistenceService;
import eu.domibus.core.alerts.model.common.AlertLevel;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.common.EventType;
import eu.domibus.core.alerts.model.service.AccountDisabledModuleConfiguration;
import eu.domibus.core.alerts.model.service.AccountDisabledMoment;
import eu.domibus.core.alerts.model.service.LoginFailureModuleConfiguration;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@RunWith(JMockit.class)
public class UserAlertsServiceImplTest {

    @Injectable
    private UserDao userDao;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private UserRoleDao userRoleDao;

    @Injectable
    protected UserPasswordHistoryDao userPasswordHistoryDao;

    @Injectable
    private UserPersistenceService userPersistenceService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private UserConverter userConverter;

    @Injectable
    private MultiDomainAlertConfigurationService alertConfiguration;

    @Injectable
    private MultiDomainAlertConfigurationService alertsConfiguration;

    @Injectable
    private EventService eventService;

    @Injectable
    protected UserDomainService userDomainService;

    @Injectable
    protected DomainService domainService;

    @Tested
    private UserAlertsServiceImpl userAlertsService;

    @Test
    public void testSendPasswordExpiredAlerts(@Mocked UserDaoBase dao) {
        final LocalDate today = LocalDate.of(2018, 10, 15);
        final Integer maxPasswordAge = 10;
        final Integer howManyDaysToGenerateAlertsAfterExpiration = 3;
        final LocalDate from = LocalDate.of(2018, 10, 2);
        final LocalDate to = LocalDate.of(2018, 10, 5);
        final User user1 = new User() {{
            setUserName("user1");
            setPassword("anypassword");
        }};
        final User user2 = new User() {{
            setUserName("user2");
            setPassword("anypassword");
        }};
        final List<User> users = Arrays.asList(user1, user2);

        new Expectations(LocalDate.class) {{
            LocalDate.now();
            result = today;
        }};
        new Expectations() {{
            userAlertsService.getAlertTypeForPasswordExpired();
            result = AlertType.PASSWORD_EXPIRED;
            alertConfiguration.getRepetitiveAlertConfiguration(AlertType.PASSWORD_EXPIRED).isActive();
            result = true;
            alertConfiguration.getRepetitiveAlertConfiguration(AlertType.PASSWORD_EXPIRED).getEventDelay();
            result = howManyDaysToGenerateAlertsAfterExpiration;
            userAlertsService.getMaximumPasswordAgeProperty();
            result = ConsoleUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE;
            domibusPropertyProvider.getOptionalDomainProperty(ConsoleUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE);
            result = maxPasswordAge.toString();
            userAlertsService.getUserDao();
            result = dao;
            dao.findWithPasswordChangedBetween(from, to, false);
            result = users;
            userAlertsService.getEventTypeForPasswordExpired();
            result = EventType.PASSWORD_EXPIRED;
        }};

        userAlertsService.triggerExpiredEvents(false);

        new VerificationsInOrder() {{
            eventService.enqueuePasswordExpirationEvent(EventType.PASSWORD_EXPIRED, (User) any, maxPasswordAge);
            times = 2;
        }};
    }

    @Test
    public void testSendPasswordImminentExpirationAlerts(@Mocked UserDaoBase dao) {
        final LocalDate today = LocalDate.of(2018, 10, 15);
        final Integer maxPasswordAge = 10;
        final Integer howManyDaysBeforeExpirationToGenerateAlerts = 4;
        final LocalDate from = LocalDate.of(2018, 10, 5);
        final LocalDate to = LocalDate.of(2018, 10, 9);
        final UserEntityBase user1 = new User() {{
            setUserName("user1");
            setPassword("anypassword");
        }};
        final UserEntityBase user2 = new User() {{
            setUserName("user2");
            setPassword("anypassword");
        }};
        final List<UserEntityBase> users = Arrays.asList(user1, user2);

        new Expectations(LocalDate.class) {{
            LocalDate.now();
            result = today;
        }};
        new Expectations() {{
            userAlertsService.getAlertTypeForPasswordImminentExpiration();
            result = AlertType.PASSWORD_IMMINENT_EXPIRATION;
            userAlertsService.getMaximumPasswordAgeProperty();
            result = ConsoleUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE;
            alertConfiguration.getRepetitiveAlertConfiguration(AlertType.PASSWORD_IMMINENT_EXPIRATION).isActive();
            result = true;
            alertConfiguration.getRepetitiveAlertConfiguration(AlertType.PASSWORD_IMMINENT_EXPIRATION).getEventDelay();
            result = howManyDaysBeforeExpirationToGenerateAlerts;
            domibusPropertyProvider.getOptionalDomainProperty(ConsoleUserAlertsServiceImpl.MAXIMUM_PASSWORD_AGE);
            result = maxPasswordAge.toString();
            userAlertsService.getEventTypeForPasswordImminentExpiration();
            result = EventType.PASSWORD_IMMINENT_EXPIRATION;
            userAlertsService.getUserDao();
            result = dao;
            dao.findWithPasswordChangedBetween(from, to, false);
            result = users;
        }};

        userAlertsService.triggerImminentExpirationEvents(false);

        new VerificationsInOrder() {{
            eventService.enqueuePasswordExpirationEvent(EventType.PASSWORD_IMMINENT_EXPIRATION, (UserEntityBase) any, maxPasswordAge);
            times = 2;
        }};
    }

    @Test
    public void testSendPasswordAlerts() {

        userAlertsService.triggerPasswordExpirationEvents();

        new VerificationsInOrder() {{
            userAlertsService.triggerImminentExpirationEvents(false);
            times = 1;
        }};
        new VerificationsInOrder() {{
            userAlertsService.triggerExpiredEvents(false);
            times = 1;
        }};
    }

    @Test
    public void triggerDisabledEventTest() {
        final User user1 = new User() {{
            setUserName("user1");
            setPassword("anypassword");
        }};
        AccountDisabledModuleConfiguration conf = new AccountDisabledModuleConfiguration(AlertType.USER_ACCOUNT_DISABLED,
                AlertLevel.MEDIUM, AccountDisabledMoment.AT_LOGON, "");
        new Expectations() {{
            alertsConfiguration.getAccountDisabledConfiguration();
            result = conf;
            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerDisabledEvent(user1);

        new VerificationsInOrder() {{
            eventService.enqueueAccountDisabledEvent(UserEntityBase.Type.CONSOLE, user1.getUserName(), (Date)any);
            times = 1;
        }};
    }

    @Test
    public void triggerLoginEventsTest() {
        final User user1 = new User() {{
            setUserName("user1");
            setPassword("anypassword");
        }};
        LoginFailureModuleConfiguration conf = new LoginFailureModuleConfiguration(AlertType.USER_LOGIN_FAILURE, AlertLevel.MEDIUM, "");
        new Expectations() {{
            userAlertsService.getLoginFailureConfiguration();
            result = conf;
            userAlertsService.getUserType();
            result = UserEntityBase.Type.CONSOLE;
        }};

        userAlertsService.triggerLoginEvents("user1", UserLoginErrorReason.BAD_CREDENTIALS);

        new VerificationsInOrder() {{
            eventService.enqueueLoginFailureEvent(UserEntityBase.Type.CONSOLE, user1.getUserName(), (Date)any, false);
            times = 1;
        }};
    }

}
