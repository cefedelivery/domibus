package eu.domibus.common.model.audit;

import eu.domibus.common.model.common.ModificationType;
import eu.domibus.common.model.common.RevisionLogicalName;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@Entity
@Table(name = "TB_ACTION_AUDIT")
@DiscriminatorValue("MESSAGE_AUDIT")
@RevisionLogicalName("Message")
public class JmsMessageAudit extends AbstractGenericAudit {


    public JmsMessageAudit() {
    }

    public JmsMessageAudit(
            final String id,
            final String userName,
            final Date revisionDate,
            final ModificationType modificationType,
            final String fromQueue,
            final String toQueue) {
        super(id, userName, revisionDate, modificationType);
        this.fromQueue = fromQueue;
        this.toQueue = toQueue;
    }

    @Column(name = "FROM_QUEUE")
    private String fromQueue;

    @Column(name = "TO_QUEUE")
    private String toQueue;

    public String getFromQueue() {
        return fromQueue;
    }

    public String getToQueue() {
        return toQueue;
    }
}
