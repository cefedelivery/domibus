package eu.domibus.web.rest.ro;

import java.util.Date;

/**
 * TestService Message object
 * @author Tiago Miguel
 * @since 4.0
 */
public class TestServiceMessageInfoRO {

    String partyId;

    String accessPoint;

    Date timeReceived;

    String messageId;

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(String accessPoint) {
        this.accessPoint = accessPoint;
    }

    public Date getTimeReceived() {
        return timeReceived;
    }

    public void setTimeReceived(Date timeReceived) {
        this.timeReceived = timeReceived;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
