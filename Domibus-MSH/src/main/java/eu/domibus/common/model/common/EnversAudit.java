package eu.domibus.common.model.common;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.hibernate.envers.RevisionType;

import javax.persistence.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * This class is used to store the type of entity modified in hibernate, the type of modification and the entity id.
 * The type of modification {@link RevisionType} and entity id are also reflected in the different audit tables , but we
 * wanted to have it centralized in order to facilitate the audit log queries.
 */
@Entity
@Table(name = "TB_REV_CHANGES")
public class EnversAudit extends AbstractBaseEntity {

    /**
     * The audited entity ID.
     */
    @Column(name = "ENTITY_ID")
    private String id;
    /**
     * The logical group name. For instance a modification to a legconfiguration will
     * be reference under configuration logical name.
     */
    @Column(name = "GROUP_NAME")
    private String groupName;
    /**
     * The name of the entity.
     */
    @Column(name = "ENTIY_NAME")
    private String entityName;
    /**
     * The type of modification of the entity.
     */
    @Column(name = "MODIFICATION_TYPE")
    @Enumerated(EnumType.STRING)
    private ModificationType modificationType;

    @Column(name = "AUDIT_ORDER")
    private Integer auditOrder;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public ModificationType getModificationType() {
        return modificationType;
    }

    public void setModificationType(ModificationType modificationType) {
        this.modificationType = modificationType;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Integer getAuditOrder() {
        return auditOrder;
    }

    public void setAuditOrder(Integer auditOrder) {
        this.auditOrder = auditOrder;
    }

    @Override
    public String toString() {
        return "EnversAudit{" +
                "id='" + id + '\'' +
                ", groupName='" + groupName + '\'' +
                ", entityName='" + entityName + '\'' +
                ", modificationType=" + modificationType +
                ", auditOrder=" + auditOrder +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EnversAudit that = (EnversAudit) o;

        if (!id.equals(that.id)) return false;
        if (!groupName.equals(that.groupName)) return false;
        if (!entityName.equals(that.entityName)) return false;
        if (modificationType != that.modificationType) return false;
        return auditOrder.equals(that.auditOrder);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + groupName.hashCode();
        result = 31 * result + entityName.hashCode();
        result = 31 * result + modificationType.hashCode();
        result = 31 * result + auditOrder.hashCode();
        return result;
    }
}
