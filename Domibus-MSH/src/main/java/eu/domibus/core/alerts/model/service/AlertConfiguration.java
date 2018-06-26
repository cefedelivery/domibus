package eu.domibus.core.alerts.model.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(AlertConfiguration.class);

    private boolean active;

    private MessagingConfiguration messagingConfiguration;



    public MessagingConfiguration getMessagingConfiguration() {
        return messagingConfiguration;
    }


    public boolean isActive() {
        return active;
    }


}
