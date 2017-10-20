package eu.domibus.common.model.audit;

import eu.domibus.common.model.common.ModificationType;
import eu.domibus.common.model.common.RevisionLogicalName;

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
public class MessageAudit extends AbstractGenericAudit {


    public MessageAudit() {
    }

    public MessageAudit(final String id, final String userName, final Date revisionDate, final ModificationType modificationType) {
        super(id, userName, revisionDate, modificationType);
    }
}
