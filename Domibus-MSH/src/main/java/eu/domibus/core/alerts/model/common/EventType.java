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

    USER_LOGIN_FAILURE(AlertType.USER_LOGIN_FAILURE, "loginFailure", AuthenticationEvent.class, true),
    USER_ACCOUNT_DISABLED(AlertType.USER_ACCOUNT_DISABLED, "accountDisabled", AuthenticationEvent.class, true),
    PLUGIN_USER_LOGIN_FAILURE(AlertType.PLUGIN_USER_LOGIN_FAILURE, "loginFailure", AuthenticationEvent.class),

    PASSWORD_EXPIRED(AlertType.PASSWORD_EXPIRED, "PASSWORD_EXPIRATION", PasswordExpirationEventProperties.class, true,
            DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PASSWORD_IMMINENT_EXPIRATION(AlertType.PASSWORD_IMMINENT_EXPIRATION, "PASSWORD_EXPIRATION", PasswordExpirationEventProperties.class, true,
            DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION),
    PLUGIN_PASSWORD_EXPIRED(AlertType.PLUGIN_PASSWORD_EXPIRED, "PASSWORD_EXPIRATION", PasswordExpirationEventProperties.class,
            DomibusMessageCode.SEC_PASSWORD_EXPIRED),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION(AlertType.PLUGIN_PASSWORD_IMMINENT_EXPIRATION, "PASSWORD_EXPIRATION", PasswordExpirationEventProperties.class,
            DomibusMessageCode.SEC_PASSWORD_IMMINENT_EXPIRATION);

    private AlertType defaultAlertType;
    private final String queueSelector;
    private Class<? extends Enum> propertiesEnumClass;
    private boolean userRelated;
    private final DomibusMessageCode securityMessageCode;

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass,
              boolean isUserRelated, DomibusMessageCode securityMessageCode) {
        this.defaultAlertType = defaultAlertType;
        this.queueSelector = queueSelector;
        this.securityMessageCode = securityMessageCode;
        this.propertiesEnumClass = propertiesEnumClass;
        this.userRelated = isUserRelated;
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass,
              boolean isUserRelated) {
        this(defaultAlertType, queueSelector, propertiesEnumClass, isUserRelated, null);
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass,
              DomibusMessageCode securityMessageCode) {
        this(defaultAlertType, queueSelector, propertiesEnumClass, false, securityMessageCode);
    }

    EventType(AlertType defaultAlertType, String queueSelector, Class<? extends Enum> propertiesEnumClass) {
        this(defaultAlertType, queueSelector, propertiesEnumClass, false, null);
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
        return this.securityMessageCode;
    }

    public boolean isUserRelated() {
        return this.userRelated;
    }
}
