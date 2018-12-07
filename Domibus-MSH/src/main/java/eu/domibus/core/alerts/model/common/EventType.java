package eu.domibus.core.alerts.model.common;

import eu.domibus.logging.DomibusMessageCode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public enum EventType {

    MSG_STATUS_CHANGED(AlertType.MSG_STATUS_CHANGED, "message", MessageEvent.class),

    CERT_IMMINENT_EXPIRATION(AlertType.CERT_IMMINENT_EXPIRATION, "certificateImminentExpiration", CertificateEvent.class),
    CERT_EXPIRED(AlertType.CERT_EXPIRED, "certificateExpired", CertificateEvent.class),

    USER_LOGIN_FAILURE(AlertType.USER_LOGIN_FAILURE, "loginFailure", AuthenticationEvent.class),
    USER_ACCOUNT_DISABLED(AlertType.USER_ACCOUNT_DISABLED, "accountDisabled", AuthenticationEvent.class),
    PLUGIN_USER_LOGIN_FAILURE(AlertType.PLUGIN_USER_LOGIN_FAILURE, "loginFailure", AuthenticationEvent.class),

    //TODO: maybe we should get rid of the plugin variant of the Event types
    PASSWORD_EXPIRED(AlertType.PASSWORD_EXPIRED, "PASSWORD_EXPIRATION", DomibusMessageCode.SEC_PASSWORD_EXPIRED,
            PasswordExpirationEventProperties.class),
    PASSWORD_IMMINENT_EXPIRATION(AlertType.PASSWORD_IMMINENT_EXPIRATION, "PASSWORD_EXPIRATION", DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION,
            PasswordExpirationEventProperties.class),
    PLUGIN_PASSWORD_EXPIRED(AlertType.PLUGIN_PASSWORD_EXPIRED, "PASSWORD_EXPIRATION", DomibusMessageCode.SEC_PASSWORD_EXPIRED,
            PasswordExpirationEventProperties.class),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION(AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION, "PASSWORD_EXPIRATION", DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION,
            PasswordExpirationEventProperties.class);

    private AlertType defaultAlertType;
    private final String queueSelector;
    private final DomibusMessageCode securityMessageCode;
    private Class<? extends Enum> propertiesEnumClass;

    EventType(AlertType defaultAlertType, String queueSelector, DomibusMessageCode securityMessageCode, Class<? extends Enum> propertiesEnumClass) {
        this.defaultAlertType = defaultAlertType;
        this.queueSelector = queueSelector;
        this.securityMessageCode = securityMessageCode;
        this.propertiesEnumClass = propertiesEnumClass;
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass) {
        this(defaultAlertType, queueSelector, null, propertiesEnumClass);
    }

    public AlertType geDefaultAlertType() {
        return this.defaultAlertType;
    }

    public List<String> getProperties() {
        ArrayList<String> list = new ArrayList<>();
        EnumSet.allOf(this.propertiesEnumClass).forEach(x -> list.add(((Enum) x).name()));
        return list;
    }

    public String getQueueSelector() {
        return this.queueSelector;
    }

    public DomibusMessageCode getSecurityMessageCode() {
        //TODO: see if we need to throw
        if (this.securityMessageCode == null) {
            throw new IllegalStateException("SecurityMessageCode for event type " + this.name() + " not defined");
        }
        return this.securityMessageCode;
    }

}
