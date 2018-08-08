package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.MailSender;
import eu.domibus.core.alerts.model.common.AlertStatus;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.MailModel;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.internet.AddressException;

import static org.junit.Assert.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class MailAlertDispatcherServiceImplTest {

    @Tested
    private MailAlertDispatcherServiceImpl mailAlertDispatcherService;

    @Injectable
    private AlertService alertService;

    @Injectable
    private MailSender mailSender;

    @Injectable
    private MultiDomainAlertConfigurationService multiDomainAlertConfigurationService;

    @Test
    public void dispatch(@Mocked final Alert alert,@Mocked final MailModel mailModelForAlert) {
        final String from = "sender.test@test.test";
        final String to = "receiver.test@test.test";
        new Expectations(){{
            alertService.getMailModelForAlert(alert);
            result=mailModelForAlert;
            multiDomainAlertConfigurationService.getCommonConfiguration().getSendFrom();
            result = from;
            multiDomainAlertConfigurationService.getCommonConfiguration().getSendTo();
            result = to;
        }};
        mailAlertDispatcherService.dispatch(alert);
        new VerificationsInOrder(){{
            alert.setAlertStatus(AlertStatus.FAILED);times=1;
            mailSender.sendMail(
                    mailModelForAlert,
                    from,
                    to);times=1;
            alert.setAlertStatus(AlertStatus.SUCCESS);times=1;
            alertService.handleAlertStatus(alert);times=1;
        }};
    }
    @Test(expected = AddressException.class)
    public void dispatchWithError(@Mocked final Alert alert,@Mocked final MailModel mailModelForAlert) {
        final String from = "sender.test@test.test";
        final String to = "receiver.test@test.test";
        new Expectations(){{
            alertService.getMailModelForAlert(alert);
            result=mailModelForAlert;
            multiDomainAlertConfigurationService.getCommonConfiguration().getSendFrom();
            result = from;
            multiDomainAlertConfigurationService.getCommonConfiguration().getSendTo();
            result = to;
            mailSender.sendMail(
                    mailModelForAlert,
                    from,
                    to);
            result=new AddressException();

        }};
        mailAlertDispatcherService.dispatch(alert);
        new VerificationsInOrder(){{
            alert.setAlertStatus(AlertStatus.FAILED);times=1;
            mailSender.sendMail(
                    mailModelForAlert,
                    from,
                    to);times=1;
            alert.setAlertStatus(AlertStatus.SUCCESS);times=0;
            alertService.handleAlertStatus(alert);times=1;
        }};
    }
}