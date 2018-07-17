package eu.domibus.core.alerts.model.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class CommonConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(CommonConfiguration.class);


    private Integer alertLifeTimeInDays;

    private String sendFrom;

    private String sendTo;

    public CommonConfiguration(Integer alertLifeTimeInDays, String sendFrom, String sendTo) {
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
