package eu.domibus.core.alerts.model.common;
/**
 * @author Thomas Dussart
 * @since 4.0
 */
public enum AlertType {
    MSG_STATUS_CHANGED("message.ftl"),
    CERT_IMMINENT_EXPIRATION("cert_imminent_expiration.ftl"),
    CERT_EXPIRED("cert_expired.ftl"),
    USER_LOGIN_FAILURE("login_failure.ftl"),
    USER_ACCOUNT_DISABLED("account_disabled.ftl"),
    PASSWORD_IMMINENT_EXPIRATION("password_imminent_expiration.ftl");

    //in the future an alert will not have one to one mapping.
    public static AlertType getAlertTypeFromEventType(EventType eventType) {
        switch (eventType) {
            case MSG_STATUS_CHANGED:
                return MSG_STATUS_CHANGED;
            case CERT_IMMINENT_EXPIRATION:
                return CERT_IMMINENT_EXPIRATION;
            case CERT_EXPIRED:
                return CERT_EXPIRED;
            case USER_LOGIN_FAILURE:
                return USER_LOGIN_FAILURE;
            case USER_ACCOUNT_DISABLED:
                return USER_ACCOUNT_DISABLED;
            case PASSWORD_IMMINENT_EXPIRATION:
                return PASSWORD_IMMINENT_EXPIRATION;

            default:
                throw new IllegalStateException("There should be a one to one mapping between alert type and event type.");
        }
    }

    private final String template;

    AlertType(String template) {
        this.template = template;

    }

    public String getTemplate() {
        return template;
    }


}
