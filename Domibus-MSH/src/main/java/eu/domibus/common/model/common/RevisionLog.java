package eu.domibus.common.model.common;

import eu.domibus.common.listener.CustomRevisionEntityListener;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Own implementation of hibernate-envers Revision entity, in order to store the user and the modification type.
 *
 * @author Thomas Dussart
 * @since 4.0
 */
@Entity
@Table(name = "TB_REV_INFO")
@RevisionEntity(CustomRevisionEntityListener.class)
public class RevisionLog extends DefaultRevisionEntity {
    /**
     * User involve in this modification
     */
    @Column(name = "USER_NAME")
    private String userName;
    /**
     * Date of the modification.
     */
    @Column(name = "REVISION_DATE")
    private Date revisionDate;
    /**
     * Different entities can be modified during the same transaction update/create/delete.
     * This field reflect the list of entities.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "TB_REV_CHANGES", joinColumns = @JoinColumn(name = "REV"))
    @Fetch(FetchMode.JOIN)
    private Set<EntityRevisionType> revisionTypes = new HashSet<>();

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }

    public void addEntityRevisionType(final String entityId, final String entityName, final String groupName, final ModificationType modificationType, final int auditOrder) {
        EntityRevisionType entityRevisionType = new EntityRevisionType();
        entityRevisionType.setGroupName(groupName);
        entityRevisionType.setEntityName(entityName);
        entityRevisionType.setModificationType(modificationType);
        entityRevisionType.setId(entityId);
        entityRevisionType.setAuditOrder(auditOrder);
        this.revisionTypes.add(entityRevisionType);
    }
}
