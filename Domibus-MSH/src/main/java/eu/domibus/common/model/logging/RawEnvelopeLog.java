
package eu.domibus.common.model.logging;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import eu.domibus.ebms3.common.model.SignalMessage;
import eu.domibus.ebms3.common.model.UserMessage;

import javax.persistence.*;

/**
 * @author idragusa
 * @since 3.2.5
 */
@Entity
@Table(name = "TB_RAWENVELOPE_LOG")
@NamedQueries({
        @NamedQuery(name = "RawDto.findByMessageId", query = "SELECT new eu.domibus.common.model.logging.RawEnvelopeDto(l.entityId,l.rawXML) FROM RawEnvelopeLog l where l.userMessage.messageInfo.messageId=:MESSAGE_ID"),
        @NamedQuery(name = "Raw.findByMessageId", query = "SELECT l FROM RawEnvelopeLog l where l.userMessage.messageInfo.messageId=:MESSAGE_ID")
})
public class RawEnvelopeLog extends AbstractBaseEntity {
    @OneToOne
    @JoinColumn(name = "USERMESSAGE_ID_FK")
    protected UserMessage userMessage;

    @OneToOne
    @JoinColumn(name = "SIGNALMESSAGE_ID_FK")
    protected SignalMessage signalMessage;

    @Lob
    @Column(name = "RAW_XML")
    protected String rawXML;


    public RawEnvelopeLog() {
    }

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(UserMessage userMessage) {
        this.userMessage = userMessage;
    }

    public SignalMessage getSignalMessage() {
        return signalMessage;
    }

    public void setSignalMessage(SignalMessage signalMessage) {
        this.signalMessage = signalMessage;
    }

    public String getRawXML() {
        return rawXML;
    }

    public void setRawXML(String rawXML) {
        this.rawXML = rawXML;
    }
}
