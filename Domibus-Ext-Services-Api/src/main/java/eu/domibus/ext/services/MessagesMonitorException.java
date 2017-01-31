package eu.domibus.ext.services;

/**
 * Specific exception for the messages monitor service
 *
 * @author Federico Martini
 * @since 3.3
 */
public class MessagesMonitorException extends RuntimeException {

    // TODO put in super class DomibusServiceException
    private String domibusErrorCode;

    public String getDomibusErrorCode() {
        return domibusErrorCode;
    }

    public void setDomibusErrorCode(String domibusErrorCode) {
        this.domibusErrorCode = domibusErrorCode;
    }

}
