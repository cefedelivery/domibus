package eu.domibus.core.alerts.configuration;

import eu.domibus.common.MessageStatus;
import eu.domibus.core.alerts.model.AlertLevel;
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

    }

    public Map<MessageStatus, AlertLevel> getMessageStatusLevels() {
        return Collections.unmodifiableMap(messageStatusLevels);
    }
}
