package eu.domibus.common.model.common;

import org.hibernate.envers.RevisionType;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * This class is used to store the type of entity modified in hibernate, the type of modification and the entity id.
 * The type of modification {@link RevisionType} and entity id are also reflected in the different audit tables , but we
 * wanted to have it centralized in order to facilitate the audit log queries.
 */
@Embeddable
public class EntityRevisionType {

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
}
