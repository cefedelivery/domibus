package eu.domibus.core.alerts.model.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class CommonConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(CommonConfiguration.class);


    private final Integer alertLifeTimeInDays;

    private final String sendFrom;

    private final String sendTo;

    public CommonConfiguration(final Integer alertLifeTimeInDays, final String sendFrom, final String sendTo) {
        this.alertLifeTimeInDays = alertLifeTimeInDays;
        this.sendFrom = sendFrom;
        this.sendTo = sendTo;
    }


    public Integer getAlertLifeTimeInDays() {
        return alertLifeTimeInDays;
    }

    public String getSendFrom() {
        return sendFrom;
    }

    public String getSendTo() {
        return sendTo;
    }
}
