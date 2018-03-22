package eu.domibus.core.message;

import eu.domibus.api.message.SignalMessageLogService;
import eu.domibus.common.MSHRole;
import eu.domibus.common.MessageStatus;
import eu.domibus.common.NotificationStatus;
import eu.domibus.common.dao.SignalMessageLogDao;
import eu.domibus.common.model.logging.SignalMessageLog;
import eu.domibus.common.model.logging.SignalMessageLogBuilder;
import eu.domibus.ebms3.common.model.Ebms3Constants;
import eu.domibus.ebms3.common.model.MessageSubtype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of Default Service for SignalMessageLog
 * @author Tiago Miguel
 * @since 4.0
 */
@Service
public class SignalMessageLogDefaultService implements SignalMessageLogService {

    @Autowired
    SignalMessageLogDao signalMessageLogDao;

    private SignalMessageLog createSignalMessageLog(String messageId, MessageSubtype messageSubtype) {
        // builds the signal message log
        SignalMessageLogBuilder smlBuilder = SignalMessageLogBuilder.create()
                .setMessageId(messageId)
                .setMessageStatus(MessageStatus.RECEIVED)
                .setMshRole(MSHRole.RECEIVING)
                .setNotificationStatus(NotificationStatus.NOT_REQUIRED)
                .setMessageSubtype(messageSubtype);

        return smlBuilder.build();
    }


    @Override
    public void save(String messageId, String userMessageService, String userMessageAction) {
        // Sets the subtype
        MessageSubtype messageSubtype = null;
        if (checkTestMessage(userMessageService, userMessageAction)) {
            messageSubtype = MessageSubtype.TEST;
        }
        // Builds the signal message log
        final SignalMessageLog signalMessageLog = createSignalMessageLog(messageId, messageSubtype);
        // Saves an entry of the signal message log
        signalMessageLogDao.create(signalMessageLog);
    }

    /**
     * Checks <code>service</code> and <code>action</code> to determine if it's a TEST message
     * @param service Service
     * @param action Action
     * @return True, if it's a test message and false otherwise
     */
    private Boolean checkTestMessage(final String service, final String action) {
        return Ebms3Constants.TEST_SERVICE.equals(service)
                && Ebms3Constants.TEST_ACTION.equals(action);

    }
}
