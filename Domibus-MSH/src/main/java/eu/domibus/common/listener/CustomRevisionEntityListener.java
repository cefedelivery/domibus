package eu.domibus.common.listener;

import eu.domibus.common.model.common.ModificationType;
import eu.domibus.common.model.common.RevisionLog;
import eu.domibus.common.model.common.RevisionLogicalName;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionType;
import org.slf4j.MDC;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import static eu.domibus.logging.DomibusLogger.MDC_USER;

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * Custom listener that allows us to add custom information to the hiberante envers schema.
 */
public class CustomRevisionEntityListener implements EntityTrackingRevisionListener {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomRevisionEntityListener.class);

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
        revisionLog.setUserName(MDC.get(MDC_USER));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void entityChanged(Class entityClass, String entityName, Serializable entityId, RevisionType revisionType, Object revisionEntity) {
        LOG.info(entityName);
        LOG.info(revisionType.name());
        ModificationType modificationType = getModificationType(revisionType);
        String logicalName = extractLogicalNameIfExist(entityClass);
        if (StringUtils.isNotEmpty(logicalName)) {
            entityName = logicalName;
        }
        ((RevisionLog) revisionEntity).addEntityRevisionType(entityName, modificationType);
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

    /**
     * The is a {@link RevisionLogicalName} enum to give a logical name to the entities that we are auditing.
     *
     * @param entityClass the audited entity.
     * @return the logical name.
     */
    private String extractLogicalNameIfExist(Class entityClass) {
        String logicalName = null;
        if (entityClass.isAnnotationPresent(RevisionLogicalName.class)) {
            Annotation annotation = entityClass.getAnnotation(RevisionLogicalName.class);
            try {
                Method method = annotation.annotationType().getDeclaredMethod("value");
                logicalName = (String) method.invoke(annotation, new Object[0]);
                return logicalName;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                LOG.error(e.getMessage(), e);
                throw new IllegalArgumentException("A problem occurred while reading RevisionLogicalName annotation");
            }
        }
        return logicalName;
    }
}
