package eu.domibus.common.model.audit;

import eu.domibus.common.model.common.ModificationType;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "TB_ACTION_AUDIT")
@DiscriminatorColumn(name = "AUDIT_TYPE")
public class AbstractGenericAudit extends AbstractBaseEntity {

    @Column(name = "ENTITY_ID")
    private String id;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "REVISION_DATE")
    private Date revisionDate;

    @Column(name = "MODIFICATION_TYPE")
    @Enumerated(EnumType.STRING)
    private ModificationType modificationType;


    public AbstractGenericAudit() {
    }

    public AbstractGenericAudit(
            final String id,
            final String userName,
            final Date revisionDate,
            final ModificationType modificationType) {
        this.id = id;
        this.userName = userName;
        this.revisionDate = revisionDate;
        this.modificationType = modificationType;
    }

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public Date getRevisionDate() {
        return revisionDate;
    }

    public ModificationType getModificationType() {
        return modificationType;
    }


}
