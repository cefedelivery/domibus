package eu.domibus.core.alerts.model.common;

public enum AlertType {
    MSG_COMMUNICATION_FAILURE("message.ftl", "domibus.alert.msg.communication_failure.mail.subject"),
    CERT_IMMINENT_EXPIRATION("cert_imminent_expiration.ftl", ""),
    CERT_EXPIRED("cert_expired.ftl", ""),
    USER_LOGIN_FAILURE("login_failure.ftl", ""),
    USER_ACCOUNT_DISABLED("account_disabled..ftl", "");

    //in the future an alert will not have one to one mapping.
    public static AlertType getAlertTypeFromEventType(EventType eventType) {
        switch (eventType) {
            case MSG_COMMUNICATION_FAILURE:
                return MSG_COMMUNICATION_FAILURE;
            case CERT_IMMINENT_EXPIRATION:
                return CERT_IMMINENT_EXPIRATION;
            case CERT_EXPIRED:
                return CERT_EXPIRED;
            case USER_LOGIN_FAILURE:
                return USER_LOGIN_FAILURE;
            case USER_ACCOUNT_DISABLED:
                return USER_ACCOUNT_DISABLED;
            default:
                throw new IllegalStateException("There should be a one to one mapping between alert type and event type.");
        }
    }

    private String template;

    private String subjectProperty;

    AlertType(String template, String subjectProperty) {
        this.template = template;
        this.subjectProperty = subjectProperty;
    }

    public String getTemplate() {
        return template;
    }

    public String getSubjectProperty() {
        return subjectProperty;
    }
}
