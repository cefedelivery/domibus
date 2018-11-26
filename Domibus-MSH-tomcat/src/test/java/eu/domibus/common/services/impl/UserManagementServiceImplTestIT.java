package eu.domibus.common.services.impl;

import eu.domibus.AbstractIT;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.common.model.security.UserLoginErrorReason;
import eu.domibus.core.alerts.dao.AlertDao;
import eu.domibus.core.alerts.listener.AuthenticatorListener;
import eu.domibus.core.alerts.model.common.AlertCriteria;
import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.core.alerts.model.persist.Alert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.with;

public class UserManagementServiceImplTestIT extends AbstractIT {

    @Autowired
    UserManagementServiceImpl userManagementService;
    @Autowired
    SuperUserManagementServiceImpl superUserManagementService;
    @Autowired
    DomainContextProvider domainContextProvider;
    @Autowired
    AlertDao alertDao;
    @Autowired
    AuthenticatorListener authenticatorListener;

    @Test
    public void triggerAlertBadCredential() {
        final String userName = "test1";
        final String superUserName = "super1";

        domainContextProvider.setCurrentDomain(DomainService.DEFAULT_DOMAIN);
        userManagementService.triggerEvent(userName, UserLoginErrorReason.BAD_CREDENTIALS);
        domainContextProvider.clearCurrentDomain();
        superUserManagementService.triggerEvent(superUserName, UserLoginErrorReason.BAD_CREDENTIALS);

        // test that we have 2 different alerts, with different properties (high vs low, etc),
        // each in the correct domain

        AlertCriteria alertCriteria = new AlertCriteria();
        alertCriteria.setPageSize(100);
        alertCriteria.setAlertType(AlertType.USER_LOGIN_FAILURE);

        domainContextProvider.setCurrentDomain(DomainService.DEFAULT_DOMAIN);
        waitUntil(() -> alertFound(alertCriteria));

        domainContextProvider.clearCurrentDomain();
        List<Alert> superAlerts = alertDao.filterAlerts(alertCriteria);
        waitUntil(() -> alertFound(alertCriteria));


    }

    private Boolean alertFound(AlertCriteria alertCriteria) {
        List<Alert> alertList = alertDao.filterAlerts(alertCriteria);
        return alertList != null && !alertList.isEmpty();
    }

    private void waitUntil(Callable<Boolean> conditionEvaluator) {
        with().pollInterval(500, TimeUnit.MILLISECONDS).await()
                .atMost(150, TimeUnit.SECONDS)
                .until(conditionEvaluator);

    }
}
