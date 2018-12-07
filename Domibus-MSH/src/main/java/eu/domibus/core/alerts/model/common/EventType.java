package eu.domibus.core.alerts.model.common;

import eu.domibus.logging.DomibusMessageCode;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public enum EventType {

    MSG_STATUS_CHANGED(AlertType.MSG_STATUS_CHANGED, "message"),
    CERT_IMMINENT_EXPIRATION(AlertType.CERT_IMMINENT_EXPIRATION, "certificateImminentExpiration"),
    CERT_EXPIRED(AlertType.CERT_EXPIRED, "certificateExpired"),
    USER_LOGIN_FAILURE(AlertType.USER_LOGIN_FAILURE, "loginFailure"),
    USER_ACCOUNT_DISABLED(AlertType.USER_ACCOUNT_DISABLED, "accountDisabled"),

    //TODO: maybe we should get rid of the plugin variant of the Event types
    PASSWORD_EXPIRED(AlertType.PASSWORD_EXPIRED, "PASSWORD_EXPIRATION", DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PASSWORD_IMMINENT_EXPIRATION(AlertType.PASSWORD_IMMINENT_EXPIRATION, "PASSWORD_EXPIRATION", DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),
    PLUGIN_PASSWORD_EXPIRED(AlertType.PLUGIN_PASSWORD_EXPIRED, "PASSWORD_EXPIRATION", DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION(AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION, "PASSWORD_EXPIRATION", DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION);

    private AlertType defaultAlertType;
    private final String queueSelector;
    private final DomibusMessageCode securityMessageCode;

    EventType(AlertType defaultAlertType, String queueSelector, DomibusMessageCode securityMessageCode) {
        this.defaultAlertType = defaultAlertType;
        this.queueSelector = queueSelector;
        this.securityMessageCode = securityMessageCode;
    }

    EventType(AlertType defaultAlertType, String queueSelector) {
        this(defaultAlertType, queueSelector, null);
    }

    public AlertType geDefaultAlertType() {
        return this.defaultAlertType;
    }

    public String getQueueSelector() {
        return this.queueSelector;
    }

    public DomibusMessageCode getSecurityMessageCode() {
        //TODO: see if we need to throw
        if (this.securityMessageCode == null)
            throw new IllegalStateException("SecurityMessageCode for event type " + this.name() + " not defined");
        return this.securityMessageCode;
    }

}
