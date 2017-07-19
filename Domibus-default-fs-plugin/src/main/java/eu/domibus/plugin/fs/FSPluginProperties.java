/*
 * Copyright 2015 e-CODEX Project
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl.html
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.plugin.fs;

import java.util.Properties;

/**
 * @author @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSPluginProperties extends Properties {

    private static final String PROPERTY_PREFIX = "fsPlugin.messages.";

    private static final String DOMAIN_PREFIX = "domain.";

    private static final String ACTION_ARCHIVE = "archive";

    private static final String ACTION_DELETE = "delete";

    private static final String LOCATION = "location";

    private static final String SENT_ACTION = "sent.action";

    private static final String SENT_PURGE_WORKER_CRONEXPRESSION = "sent.purge.worker.cronExpression";

    private static final String SENT_PURGE_EXPIRED = "sent.purge.expired";

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
        return getDomainProperty(domain, SENT_ACTION, ACTION_ARCHIVE);
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

    private String getDomainProperty(String domain, String propertyName, String defaultValue) {
        String domainFullPropertyName = PROPERTY_PREFIX + DOMAIN_PREFIX + domain + "." + propertyName;
        if (containsKey(domainFullPropertyName)) {
            return getProperty(domainFullPropertyName, defaultValue);
        }
        return getProperty(PROPERTY_PREFIX + propertyName, defaultValue);
    }

}
