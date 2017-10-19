package eu.domibus.common.model.common;

import eu.domibus.common.listener.CustomRevisionEntityListener;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RevisionLog.class);
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
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "REV")
    @Fetch(FetchMode.JOIN)
    private Set<EnversAudit> revisionTypes = new HashSet<>();

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

    /**
     * Hibernate is going to send notifications to this revision class each time an update occurs on an entity or its sub-hierarchy,
     * but in the audit system we only want to keep track of the changes at a highber level. If wee need more info it is possible to go into envers audit tables.
     *
     * @param entityId
     * @param entityName
     * @param groupName
     * @param modificationType
     * @param auditOrder
     */
    public void addEntityAudit(final String entityId, final String entityName, final String groupName, final ModificationType modificationType, final int auditOrder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding envers audit " + entityId + " entity name " + entityName + " groupName " + groupName + " modificationType " + modificationType + " auditOrder " + auditOrder);
        }
        EnversAudit enversAudit = new EnversAudit();
        enversAudit.setGroupName(groupName);
        enversAudit.setEntityName(entityName);
        enversAudit.setModificationType(modificationType);
        enversAudit.setId(entityId);
        enversAudit.setAuditOrder(auditOrder);
        revisionTypes.add(enversAudit);


        //remove every entry with higher audit order. (Eg: when you update a PMODE, you also update a PARTY, but in the audit system you juste want to see update pmode).
        Optional<Integer> min = this.revisionTypes.stream().map(EnversAudit::getAuditOrder).min(Integer::compareTo);
        min.ifPresent(integer -> revisionTypes.removeIf(r -> r.getAuditOrder() > integer));

        //Group Audit entity by id in order to remove modification in case of an addition in the samre revision.
        Map<String, List<EnversAudit>> collect = revisionTypes.stream()
                .collect(Collectors.groupingBy(e -> e.getId()));
        for (List<EnversAudit> enversAudits : collect.values()) {
            min = enversAudits.stream().map(EnversAudit::getModificationType).map(ModificationType::getOrder).min(Integer::compareTo);
            //Keep only the one with higher order, the one that we want to elinate.
            min.ifPresent(integer -> enversAudits.removeIf(r -> r.getModificationType().getOrder() == integer));
            revisionTypes.removeAll(enversAudits);
        }
    }

    public Set<EnversAudit> getRevisionTypes() {
        return Collections.unmodifiableSet(revisionTypes);
    }
}
