package eu.domibus.core.alerts.model.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    PLUGIN_USER_LOGIN_FAILURE("login_failure.ftl"),
    PASSWORD_IMMINENT_EXPIRATION("password_imminent_expiration.ftl", "domibus.alert.password.imminent_expiration", "Password imminent expiration"),
    PASSWORD_EXPIRED("password_expired.ftl", "domibus.alert.password.expired", "Password expired"),
    PLUGIN_PASSWORD_IMMINENT_EXPIRATION("password_imminent_expiration.ftl", "domibus.alert.plugin_password.imminent_expiration", "Plugin password imminent expiration"),
    PLUGIN_PASSWORD_EXPIRED("password_expired.ftl", "domibus.alert.plugin_password.expired", "Plugin password expired");

    private final String template;
    private final String configurationProperty;
    private final String title;

    AlertType(String template, String configurationProperty, String title) {
        this.template = template;
        this.configurationProperty = configurationProperty;
        this.title = title;
    }

    AlertType(String template) {
        this(template, null, null);
    }

    //in the future an alert will not have one to one mapping.
    public static AlertType getByEventType(EventType eventType) {
        return eventType.geDefaultAlertType();
    }

    public List<EventType> getSourceEvents() {
        return Arrays.stream(EventType.values()).filter(el -> el.geDefaultAlertType() == this).collect(Collectors.toList());
    }

    public String getTemplate() {
        return template;
    }

    public String getConfigurationProperty() {
        return configurationProperty;
    }

    public String getTitle() {
        return title;
    }
}
