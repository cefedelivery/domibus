package eu.domibus.ebms3.receiver;

import eu.domibus.common.model.configuration.LegConfiguration;

import javax.xml.soap.SOAPMessage;

/**
 * Created by dussath on 6/7/17.
 *
 */
public class UserMessageHandlerContext {
    private String messageId;
    private LegConfiguration legConfiguration;
    private boolean pingMessage;

    public UserMessageHandlerContext() {

    }

    public String getMessageId() {
        return messageId;
    }

    public LegConfiguration getLegConfiguration() {
        return legConfiguration;
    }



    public boolean isPingMessage() {
        return pingMessage;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setLegConfiguration(LegConfiguration legConfiguration) {
        this.legConfiguration = legConfiguration;
    }

    public void setPingMessage(boolean pingMessage) {
        this.pingMessage = pingMessage;
    }
}
