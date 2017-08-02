package eu.domibus.plugin.fs;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSPluginProperties extends Properties {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSPluginProperties.class);

    private static final String PROPERTY_PREFIX = "fsplugin.messages.";

    private static final String DOMAIN_PREFIX = "fsplugin.domains.";
    
    private static final String MESSAGES_SECTION = ".messages.";

    private static final String ACTION_ARCHIVE = "archive";

    private static final String ACTION_DELETE = "delete";

    private static final String LOCATION = "location";

    private static final String SENT_ACTION = "sent.action";

    private static final String SENT_PURGE_WORKER_CRONEXPRESSION = "sent.purge.worker.cronExpression";

    private static final String SENT_PURGE_EXPIRED = "sent.purge.expired";

    private static final String USER = "user";

    private static final String PASSWORD = "password";
    
    private Set<String> domains;

    public Set<String> getDomains() {
        if (domains == null) {
            domains = readDomains();
        }
        
        return domains;
    }

    private Set<String> readDomains() {
        Set<String> tempDomains = new LinkedHashSet<>();
         
        for (String propName : this.stringPropertyNames()) {
            if (propName.startsWith(DOMAIN_PREFIX)) {
                LOG.debug("Found domain property {}", propName);
                
                String domain = extractDomainName(propName);
                
                if (!tempDomains.contains(domain)) {
                    tempDomains.add(domain);
                    LOG.debug("Found domain {}", domain);
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
        return getSentPurgeWorkerCronExpression(null);
    }

    /**
     * @param domain The domain property qualifier
     * @return See {@link FSPluginProperties#getSentPurgeWorkerCronExpression()}
     */
    public String getSentPurgeWorkerCronExpression(String domain) {
        return getDomainProperty(domain, SENT_PURGE_WORKER_CRONEXPRESSION, "0/60 * * * * ?");
    }

    /**
     * @return The time interval (seconds) to purge sent messages
     */
    public int getSentPurgeExpired() {
        return getSentPurgeExpired(null);
    }

    /**
     * @param domain The domain property qualifier
     * @return See {@link FSPluginProperties#getSentPurgeExpired()}
     */
    public int getSentPurgeExpired(String domain) {
        return Integer.parseInt(getDomainProperty(domain, SENT_PURGE_EXPIRED, "600"));
    }

    public String getUser(String domain) {
        return getDomainProperty(domain, USER, null);
    }

    public String getPassword(String domain) {
        return getDomainProperty(domain, PASSWORD, null);
    }

    private String getDomainProperty(String domain, String propertyName, String defaultValue) {
        String domainFullPropertyName = DOMAIN_PREFIX + domain + MESSAGES_SECTION + propertyName;
        if (containsKey(domainFullPropertyName)) {
            return getProperty(domainFullPropertyName, defaultValue);
        }
        return getProperty(PROPERTY_PREFIX + propertyName, defaultValue);
    }

}
