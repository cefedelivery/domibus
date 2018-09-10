package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.MailModel;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class MailAlertDispatcherServiceImplTest {

    @Tested
    private AlertDispatcherServiceImpl alertDispatcherService;

    @Injectable
    private AlertService alertService;

    @Injectable
    protected AlertMethodFactory alertMethodFactory;

    @Test
    public void dispatch(@Mocked final Alert alert) {
        new Expectations(){{
        }};
        alertDispatcherService.dispatch(alert);

        new VerificationsInOrder(){{
            alert.setAlertStatus(AlertStatus.FAILED);
            times=1;

            alertMethodFactory.getAlertMethod().sendAlert(alert);
            times=1;

            alert.setAlertStatus(AlertStatus.SUCCESS);times=1;
            alertService.handleAlertStatus(alert);times=1;
        }};
    }

    @Test(expected = RuntimeException.class)
    public void dispatchWithError(@Mocked final Alert alert,@Mocked final MailModel mailModelForAlert) {
        new Expectations(){{
            alertMethodFactory.getAlertMethod().sendAlert(alert);
            result = new RuntimeException("Error sending alert");
        }};
        alertDispatcherService.dispatch(alert);

        new VerificationsInOrder(){{
            alert.setAlertStatus(AlertStatus.FAILED);
            times=1;

            alertMethodFactory.getAlertMethod().sendAlert(alert);
            times=1;

            alert.setAlertStatus(AlertStatus.SUCCESS);times=0;
            alertService.handleAlertStatus(alert);times=1;
        }};
    }
}