package eu.domibus.core.alerts.model.service;

import eu.domibus.core.alerts.model.common.AlertType;
import eu.domibus.logging.DomibusLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

@Component
public class RepetitiveAlertConfigurationHolder {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(RepetitiveAlertConfigurationHolder.class);

    @Autowired
    ApplicationContext applicationContext;

    private final HashMap<AlertType, ConfigurationLoader<RepetitiveAlertModuleConfiguration>> configurations = new HashMap<>();

    public ConfigurationLoader<RepetitiveAlertModuleConfiguration> get(AlertType alertType) {
        LOG.debug("Retrieving repetitive alert configuration for alert type :[{}]", alertType);
        if (this.configurations.get(alertType) == null) {
            synchronized (this.configurations) {
                if (this.configurations.get(alertType) == null) {
                    LOG.debug("Creating repetitive alert configuration for alert type :[{}]", alertType);
                    ConfigurationLoader<RepetitiveAlertModuleConfiguration> bean = applicationContext.getBean(ConfigurationLoader.class);
                    this.configurations.put(alertType, bean);
                }
            }
        }
        return configurations.get(alertType);
    }
}



