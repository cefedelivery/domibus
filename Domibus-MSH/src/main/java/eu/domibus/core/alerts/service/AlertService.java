package eu.domibus.core.alerts.service;

import eu.domibus.core.alerts.model.common.AlertCriteria;
import eu.domibus.core.alerts.model.service.Alert;
import eu.domibus.core.alerts.model.service.Event;
import eu.domibus.core.alerts.model.service.MailModel;

import java.util.List;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface AlertService {

    /**
     * Decides whether or not an alert should be created based on an event.
     * @param event the event.
     * @return the created alert.
     */
    Alert createAlertOnEvent(Event event);

    /**
     * Add alert to the alert/event monitoring queue.
     * @param alert the alert to be added.
     */
    void enqueueAlert(Alert alert);

    /**
     * Based on a alert, creates a mail model containing the information to select and fill in the mail template.
     * @param alert the alert.
     * @return the mailModel with data and template information.
     */
    MailModel getMailModelForAlert(Alert alert);

    /**
     * Manage the status of the alert after a sending tentative.
     * @param alert the alert.
     */
    void handleAlertStatus(Alert alert);

    /**
     * Call by the alert job. Try to resend failed alerts.
     */
    void retry();

    /**
     * Filter alerts based on criteria.
     * @param alertCriteria the alert criteria.
     * @return the filtered list of alerts.
     */
    List<Alert> findAlerts(AlertCriteria alertCriteria);

    /**
     * Count alerts for a given criteria.
     * @param alertCriteria the alert criteria.
     * @return the number of alerts corresponding to the criteria.
     */
    Long countAlerts(AlertCriteria alertCriteria);

    /**
     * Clean alerts which have an expired lifetime base on properties.
     */
    void cleanAlerts();

    /**
     * Set alerts as processed true/false
     * @param alerts the alerts to update.
     */
    void updateAlertProcessed(List<Alert> alerts);
}
