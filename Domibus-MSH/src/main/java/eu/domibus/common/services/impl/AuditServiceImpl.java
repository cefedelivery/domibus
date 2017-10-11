package eu.domibus.common.services.impl;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.common.dao.AuditDao;
import eu.domibus.common.services.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 * {@inheritDoc}
 */
@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditDao auditDao;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuditLog> listAudit(Set<String> auditTargets,
                                    Set<String> actions,
                                    Set<String> users,
                                    Date from,
                                    Date to,
                                    int start,
                                    int max) {
        return domainCoreConverter.convert(auditDao.listAudit(
                auditTargets,
                actions,
                users,
                from,
                to,
                start,
                max), AuditLog.class);
    }
}
