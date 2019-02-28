
package eu.domibus.common;

/**
 * The types of notifications that can be submitted to a backoffice application.
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public enum NotificationType {
    MESSAGE_FRAGMENT_RECEIVED,
    MESSAGE_RECEIVED,
    MESSAGE_FRAGMENT_SEND_FAILURE,
    MESSAGE_SEND_FAILURE,
    MESSAGE_FRAGMENT_RECEIVED_FAILURE,
    MESSAGE_RECEIVED_FAILURE,
    MESSAGE_SEND_SUCCESS,
    MESSAGE_STATUS_CHANGE;
}
