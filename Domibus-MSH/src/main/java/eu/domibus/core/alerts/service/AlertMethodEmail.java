package eu.domibus.core.alerts.service;

import eu.domibus.common.dao.security.UserDao;
import eu.domibus.common.model.security.User;
import eu.domibus.core.alerts.MailSender;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.MailModel;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

/**
 * @author Thomas Dussart, Cosmin Baciu
 * @since 4.0
 */
@Service
public class AlertMethodEmail implements AlertMethod {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(AlertMethodEmail.class);

    @Autowired
    private AlertService alertService;

    @Autowired
    private MailSender mailSender;

    @Autowired
    private MultiDomainAlertConfigurationService alertConfigurationService;

    @Autowired
    private UserDao userDao;

    @Override
    public void sendAlert(Alert alert) {
        final MailModel mailModelForAlert = alertService.getMailModelForAlert(alert);
        LOG.debug("Sending alert by email [{}]", alert);

        String from = alertConfigurationService.getCommonConfiguration().getSendFrom();
        String to = alertConfigurationService.getCommonConfiguration().getSendTo();
        mailSender.sendMail(mailModelForAlert, from, to);

        //if the alert is created form an event related to a user, send the email to the user address also
        Stream<Event> userEvents = alert.getEvents().stream().filter(event -> event.getType().isUserRelated());
        userEvents.forEach(event -> {
            //TODO: think of a way to ensure by compiler that all events have "USER" field
            String userName = event.findStringProperty("USER").get();
            if (StringUtils.isEmpty(userName)) {
                return;
            }
            User user = userDao.loadUserByUsername(userName);
            if (user == null) {
                return;
            }

            mailSender.sendMail(mailModelForAlert, from, user.getEmail());
        });
    }
}
