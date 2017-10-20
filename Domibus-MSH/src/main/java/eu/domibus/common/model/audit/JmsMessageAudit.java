package eu.domibus.common.model.audit;

import eu.domibus.common.model.common.ModificationType;
import eu.domibus.common.model.common.RevisionLogicalName;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;


/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * Entity used to track actions on the Admin console Jms Monitoring page.
 *
 */

@Entity
@DiscriminatorValue("Jms message")
@RevisionLogicalName("Jms message")
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
