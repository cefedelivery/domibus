package eu.domibus.ebms3.receiver;

import eu.domibus.common.model.configuration.LegConfiguration;

/**
 * @author Thomas Dussart
 * @since 3.3
 *
 */
public class UserMessageHandlerContext {
    private String messageId;
    private LegConfiguration legConfiguration;
    private boolean testMessage;

    public UserMessageHandlerContext() {
        // empty constructor
    }

    public String getMessageId() {
        return messageId;
    }

    public LegConfiguration getLegConfiguration() {
        return legConfiguration;
    }



    public boolean isTestMessage() {
        return testMessage;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setLegConfiguration(LegConfiguration legConfiguration) {
        this.legConfiguration = legConfiguration;
    }

    public void setTestMessage(boolean testMessage) {
        this.testMessage = testMessage;
    }
}
