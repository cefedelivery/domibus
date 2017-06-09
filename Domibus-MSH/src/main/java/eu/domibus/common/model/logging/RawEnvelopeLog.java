
package eu.domibus.common.model.logging;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.ErrorResult;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.Error;
import org.hibernate.annotations.Cascade;
import org.w3c.dom.Element;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static eu.domibus.common.model.configuration.Process.*;

/**
 * @author idragusa
 * @since 3.2.5
 */
@Entity
@Table(name = "TB_RAWENVELOPE_LOG")
/*@NamedQueries({
        @NamedQuery(name = RETRIEVE_FROM_MESSAGE_CONTEXT, query = "SELECT p FROM Process as p left join p.agreement as a left join p.legs as l left join p.initiatorParties init left join p.responderParties resp  where l.action.name=:action and l.service.name=:service and (a is null  or a.name=:agreement) and l.name=:leg and init.name=:initiatorName and resp.name=:responderName")})*/
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
