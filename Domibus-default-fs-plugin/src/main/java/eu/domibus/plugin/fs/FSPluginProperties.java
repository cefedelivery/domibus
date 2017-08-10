package eu.domibus.plugin.fs;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * File System Plugin Properties
 *
 * All the plugin configurable properties must be accessed and handled through this component.
 *
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 */
@Component
public class FSPluginProperties {

    private static final String PROPERTY_PREFIX = "fsplugin.messages.";

    private static final String DOMAIN_PREFIX = "fsplugin.domains.";
    
    private static final String MESSAGES_SECTION = ".messages.";

    private static final String LOCATION = "location";

    private static final String SENT_ACTION = "sent.action";

    private static final String SENT_PURGE_WORKER_CRONEXPRESSION = "sent.purge.worker.cronExpression";

    private static final String SENT_PURGE_EXPIRED = "sent.purge.expired";

    private static final String FAILED_ACTION = "failed.action";

    private static final String FAILED_PURGE_WORKER_CRONEXPRESSION = "failed.purge.worker.cronExpression";

    private static final String FAILED_PURGE_EXPIRED = "failed.purge.expired";

    private static final String RECEIVED_PURGE_EXPIRED = "received.purge.expired";

    private static final String RECEIVED_PURGE_WORKER_CRONEXPRESSION = "received.purge.worker.cronExpression";

    private static final String USER = "user";

    private static final String PASSWORD = "password";

    private static final String EXPRESSION = "expression";

    @Resource(name = "fsPluginProperties")
    private Properties properties;
    
    private Set<String> domains;

    public static final String ACTION_DELETE = "delete";

    public static final String ACTION_ARCHIVE = "archive";

    // TODO check domains order - is is the properties order?
    // they need to be evaluated first in order to determine if the message belongs to a specific domain.
    public Set<String> getDomains() {
        if (domains == null) {
            domains = readDomains();
        }
        return domains;
    }

    /**
     * @return The location of the directory that the plugin will use to manage the messages to be sent and received
     * in case no domain expression matches
     */
    public String getLocation() {
        return getLocation(null);
    }

    /**
     * @param domain The domain property qualifier
     * @return See {@link FSPluginProperties#getLocation()}
     */
    public String getLocation(String domain) {
        return getDomainProperty(domain, LOCATION, System.getProperty("java.io.tmpdir"));
    }

    /**
     * @return The plugin action when message is sent successfully from C2 to C3 ('delete' or 'archive')
     */
    public String getSentAction() {
        return getSentAction(null);
    }

    /**
     * @param domain The domain property qualifier
     * @return See {@link FSPluginProperties#getSentAction()}
     */
    public String getSentAction(String domain) {
        return getDomainProperty(domain, SENT_ACTION, ACTION_DELETE);
    }

    /**
     * @return The cron expression that defines the frequency of the sent messages purge job
     */
    public String getSentPurgeWorkerCronExpression() {
        return properties.getProperty(PROPERTY_PREFIX + SENT_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @return The time interval (seconds) to purge sent messages
     */
    public Integer getSentPurgeExpired() {
        return getSentPurgeExpired(null);
    }

    /**
     * @param domain The domain property qualifier
     * @return See {@link FSPluginProperties#getSentPurgeExpired()}
     */
    public Integer getSentPurgeExpired(String domain) {
        String value = getDomainProperty(domain, SENT_PURGE_EXPIRED, "600");
        return StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : null;
    }

    /**
     * @return The plugin action when message fails
     */
    public String getFailedAction() {
        return getFailedAction(null);
    }

    /**
     * @param domain The domain property qualifier
     * @return See {@link FSPluginProperties#getFailedAction()}
     */
    public String getFailedAction(String domain) {
        return getDomainProperty(domain, FAILED_ACTION, ACTION_DELETE);
    }

    /**
     * @return The cron expression that defines the frequency of the failed messages purge job
     */
    public String getFailedPurgeWorkerCronExpression() {
        return properties.getProperty(PROPERTY_PREFIX + FAILED_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @return The time interval (seconds) to purge failed messages
     */
    public Integer getFailedPurgeExpired() {
        return getFailedPurgeExpired(null);
    }

    /**
     * @param domain The domain property qualifier
     * @return See {@link FSPluginProperties#getFailedPurgeExpired()}
     */
    public Integer getFailedPurgeExpired(String domain) {
        String value = getDomainProperty(domain, FAILED_PURGE_EXPIRED, "600");
        return StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : null;
    }

    /**
     * @return The time interval (seconds) to purge received messages
     */
    public Integer getReceivedPurgeExpired() {
        return getReceivedPurgeExpired(null);
    }

    /**
     * @param domain The domain property qualifier
     * @return See {@link FSPluginProperties#getReceivedPurgeExpired()}
     */
    public Integer getReceivedPurgeExpired(String domain) {
        String value = getDomainProperty(domain, RECEIVED_PURGE_EXPIRED, "600");
        return StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : null;
    }

    /**
     * @return The cron expression that defines the frequency of the received messages purge job
     */
    public String getReceivedPurgeWorkerCronExpression() {
        return properties.getProperty(PROPERTY_PREFIX + RECEIVED_PURGE_WORKER_CRONEXPRESSION);
    }

    /**
     * @param domain The domain property qualifier
     * @return the user used to access the location specified by the property
     */
    public String getUser(String domain) {
        return getDomainProperty(domain, USER, null);
    }

    /**
     * @param domain The domain property qualifier
     * @return the password used to access the location specified by the property
     */
    public String getPassword(String domain) {
        return getDomainProperty(domain, PASSWORD, null);
    }

    Properties getProperties() {
        return properties;
    }

    void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * @param domain The domain expression property qualifier
     * @return the regex expression used to determine the domain location
     */
    public String getExpression(String domain) {
        return getDomainProperty(domain, EXPRESSION, null);
    }

    private String getDomainProperty(String domain, String propertyName, String defaultValue) {
        String domainFullPropertyName = DOMAIN_PREFIX + domain + MESSAGES_SECTION + propertyName;
        if (properties.containsKey(domainFullPropertyName)) {
            return properties.getProperty(domainFullPropertyName, defaultValue);
        }
        return properties.getProperty(PROPERTY_PREFIX + propertyName, defaultValue);
    }

    private Set<String> readDomains() {
        Set<String> tempDomains = new LinkedHashSet<>();

        for (String propName : properties.stringPropertyNames()) {
            if (propName.startsWith(DOMAIN_PREFIX)) {
                String domain = extractDomainName(propName);
                if (!tempDomains.contains(domain)) {
                    tempDomains.add(domain);
                }
            }
        }

        return tempDomains;
    }

    private String extractDomainName(String propName) {
        String unprefixedProp = StringUtils.removeStart(propName, DOMAIN_PREFIX);
        String domain = StringUtils.substringBefore(unprefixedProp, ".");
        return domain;
    }

}
