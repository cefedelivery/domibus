package eu.domibus.acknowledge.services;

import eu.domibus.acknowledge.dao.IMessageAcknowledgeDao;
import eu.domibus.api.acknowledge.IMessageAcknowledgeService;
import eu.domibus.api.acknowledge.MessageAcknowledge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by migueti on 15/03/2017.
 */
@Service(value = "messageAcknowledgeService")
public class MessageAcknowledgeDefaultService implements IMessageAcknowledgeService {

    @Autowired
    IMessageAcknowledgeDao messageAcknowledgeDao;

    public void acknowledgeMessage(String messageId) {

    }

    public List<MessageAcknowledge> getAcknowledgeMessages(String messageId) {
        return new ArrayList<>();
    }
}
