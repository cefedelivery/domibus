package eu.domibus.common.model.audit;

import eu.domibus.common.model.common.ModificationType;
import eu.domibus.common.model.common.RevisionLogicalName;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Date;


/**
 * @author Joze Rihtarsic
 * @since 4.0
 *
 * Entity used to track actions on the Pmode download actions
 */
@Entity
@DiscriminatorValue("Pmode")
@RevisionLogicalName("Pmode")
public class PModeAudit extends AbstractGenericAudit {


    public PModeAudit() {
    }

    public PModeAudit(
            final String id,
            final String userName,
            final Date revisionDate,
            final ModificationType modificationType) {
        super(id, userName, revisionDate, modificationType);
    }
}
