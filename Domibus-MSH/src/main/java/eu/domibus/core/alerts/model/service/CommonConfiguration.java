package eu.domibus.core.alerts.model.service;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class CommonConfiguration {

    private final Integer alertLifeTimeInDays;

    private final String sendFrom;

    private final String sendTo;

    public CommonConfiguration(final Integer alertLifeTimeInDays, final String sendFrom, final String sendTo) {
        this.alertLifeTimeInDays = alertLifeTimeInDays;
        this.sendFrom = sendFrom;
        this.sendTo = sendTo;
    }

    public CommonConfiguration(final Integer alertLifeTimeInDays) {
        this(alertLifeTimeInDays,"","");
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
