package eu.domibus.core.replication;

/**
 * It mpas different types of notification for replicating the data into {@code TB_MESSAGE_UI} table
 *
 * @author Catalin Enache
 * @since 4.0
 */
public enum UIJMSType {

    USER_MESSAGE_RECEIVED,
    USER_MESSAGE_SUBMITTED,
    MESSAGE_STATUS_CHANGE,
    MESSAGE_NOTIFICATION_STATUS_CHANGE,
    MESSAGE_CHANGE,

    SIGNAL_MESSAGE_SUBMITTED,
    SIGNAL_MESSAGE_RECEIVED

}
