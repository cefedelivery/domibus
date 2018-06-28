package eu.domibus.core.replication;

import eu.domibus.common.MessageStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class UIReplicationDataService {

    @Autowired
    UIMessageDao uiMessageDao;


    public void updateMessageReceived(String messageId) {
        //TODO
        //call uiMessageDao
    }


    public void updateMessageStatusChange(String messageId, MessageStatus newStatus) {
        //TODO
        //call uiMessageDao
    }
}
