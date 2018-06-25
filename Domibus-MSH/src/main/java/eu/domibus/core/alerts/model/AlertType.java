package eu.domibus.core.alerts.model;

public enum AlertType {
    MSG_COMMUNICATION_FAILURE,
    CERT_IMMINENT_EXPIRATION,
    CERT_EXPIRED,
    USER_LOGIN_FAILURE,
    USER_ACCOUNT_DISABLED;
    //in the future an alert will not have one to one mapping.
    public static AlertType getAlertTypeFromEventType(EventType eventType){
        switch (eventType){
            case MSG_COMMUNICATION_FAILURE:return MSG_COMMUNICATION_FAILURE;
            case CERT_IMMINENT_EXPIRATION:return CERT_IMMINENT_EXPIRATION;
            case CERT_EXPIRED:return CERT_EXPIRED;
            case USER_LOGIN_FAILURE:return USER_LOGIN_FAILURE;
            case USER_ACCOUNT_DISABLED:return USER_ACCOUNT_DISABLED;
            default:throw new IllegalStateException("There should be a one to one mapping between alert type and event type.");
        }
    }
}
