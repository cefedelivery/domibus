package eu.domibus.core.alerts.model.service;

import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.common.AlertLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessagingConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(MessagingConfiguration.class);

    private boolean messageCommunicationActive;

    private Map<MessageStatus,AlertLevel> messageStatusLevels=new HashMap<>();


    public MessagingConfiguration(boolean messageCommunicationActive) {
        this.messageCommunicationActive = messageCommunicationActive;
    }

    public void addStatusLevelAssociation(MessageStatus messageStatus, AlertLevel alertLevel){
        messageStatusLevels.put(messageStatus,alertLevel);
    }

    public Map<MessageStatus, AlertLevel> getMessageStatusLevels() {
        return Collections.unmodifiableMap(messageStatusLevels);
    }

    public boolean shouldMonitorMessageStatus(MessageStatus messageStatus) {
        return messageCommunicationActive && messageStatusLevels.get(messageStatus)!=null;
    }

    public AlertLevel getAlertLevel(MessageStatus messageStatus) {
        return messageStatusLevels.get(messageStatus);
    }
}
