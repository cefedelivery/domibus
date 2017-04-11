package eu.domibus.common.model.logging;

import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.ebms3.common.model.MessageType;

import javax.persistence.*;
import java.util.Date;


/**
 * @author Federico Martini
 * @since 3.2
 */
@Entity
@Table(name = "TB_MESSAGE_LOG")
@DiscriminatorValue("SIGNAL_MESSAGE")
@NamedQueries({
        @NamedQuery(name = "SignalMessageLog.findByMessageId", query = "select signalMessageLog from SignalMessageLog signalMessageLog where signalMessageLog.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "SignalMessageLog.findByMessageIdAndRole", query = "select signalMessageLog from SignalMessageLog signalMessageLog where signalMessageLog.messageId=:MESSAGE_ID and signalMessageLog.mshRole=:MSH_ROLE")
})
public class SignalMessageLog extends MessageLog {

    @ManyToOne
    @JoinColumn(name = "MESSAGE_ID", referencedColumnName = "MESSAGE_ID", updatable = false, insertable = false)
    protected MessageInfo messageInfo;

    public SignalMessageLog() {
        setMessageType(MessageType.SIGNAL_MESSAGE);
        setReceived(new Date());
        setNextAttempt(getReceived());
    }


}


