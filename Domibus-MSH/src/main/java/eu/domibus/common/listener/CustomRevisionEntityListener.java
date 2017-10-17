package eu.domibus.common.listener;

import eu.domibus.common.model.common.ModificationType;
import eu.domibus.common.model.common.RevisionLog;
import eu.domibus.common.model.common.RevisionLogicalName;
import eu.domibus.common.util.AnnotationsUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Custom listener that allows us to add custom information to the hiberante envers schema.
 */
public class CustomRevisionEntityListener implements EntityTrackingRevisionListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomRevisionEntityListener.class);

    private AnnotationsUtil annotationsUtil;

    public CustomRevisionEntityListener() {
        this.annotationsUtil = new AnnotationsUtil();
    }

    /**
     * Call when an new revision is created.
     * New revision are create one per transaction for every audited entity change.
     *
     * @param revision the new revision.
     */
    @Override
    public void newRevision(Object revision) {
        RevisionLog revisionLog = (RevisionLog) revision;
        revisionLog.setRevisionDate(new Date(System.currentTimeMillis()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            revisionLog.setUserName(authentication.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void entityChanged(Class entityClass, String entityName, Serializable entityId, RevisionType revisionType, Object revisionEntity) {
        Optional<String> logicalName = annotationsUtil.getValue(entityClass, RevisionLogicalName.class);
        ((RevisionLog) revisionEntity).addEntityRevisionType(entityId.toString(),
                entityName,
                logicalName.orElse(entityName),
                getModificationType(revisionType)
        );
    }

    /**
     * Does a mapping between envers RevisionType and our own ModificationType enum.
     *
     * @param revisionType the envers enum.
     * @return our modification enum..
     */
    private ModificationType getModificationType(RevisionType revisionType) {
        switch (revisionType) {
            case ADD:
                return ModificationType.ADD;
            case DEL:
                return ModificationType.DEL;
            case MOD:
                return ModificationType.MOD;
            default:
                String msg = "RevisionType " + revisionType + " is unknown";
                LOG.error(msg);
                throw new IllegalArgumentException(msg);
        }
    }


}
