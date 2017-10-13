package eu.domibus.common.services.impl;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.common.dao.AuditDao;
import eu.domibus.common.model.common.RevisionLogicalName;
import eu.domibus.common.services.AuditService;
import eu.domibus.common.util.AnnotationsUtil;
import eu.domibus.core.converter.DomainCoreConverter;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 * {@inheritDoc}
 * <p>
 * Service in charge of retriving audit logs, audit targets, etc...
 */
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditDao auditDao;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    @Autowired
    private AnnotationsUtil annotationsUtil;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuditLog> listAudit(
            Set<String> auditTargets,
            Set<String> actions,
            Set<String> users,
            Date from,
            Date to,
            int start,
            int max) {
        return domainCoreConverter.convert(
                auditDao.listAudit(
                        auditTargets,
                        actions,
                        users,
                        from,
                        to,
                        start,
                        max), AuditLog.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable("auditTarget")
    public List<String> listAuditTarget() {
        Set<Class<?>> typesAnnotatedWith = new Reflections("eu.domibus").
                getTypesAnnotatedWith(RevisionLogicalName.class);
        return typesAnnotatedWith.stream().
                map(aClass -> annotationsUtil.getValue(aClass, RevisionLogicalName.class)).
                //check if present is needed because the set contains subclasses that do not contain the annotation.
                        filter(Optional::isPresent).
                        map(Optional::get).
                        distinct().
                        collect(Collectors.toList());
    }
}
