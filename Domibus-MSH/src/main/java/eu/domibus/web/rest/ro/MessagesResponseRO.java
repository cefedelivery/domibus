package eu.domibus.web.rest.ro;

import eu.domibus.api.jms.JmsMessage;

import java.util.List;

/**
 * Created by musatmi on 15/05/2017.
 */
public class MessagesResponseRO {

    private List<JmsMessage> messages;

    public List<JmsMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<JmsMessage> messages) {
        this.messages = messages;
    }
}
