package eu.domibus.web.rest;

import eu.domibus.api.audit.AuditCriteria;
import eu.domibus.common.services.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.AuditResponseRo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * Rest entry point to retrieve the audit logs.
 */
@RestController
@RequestMapping(value = "/rest/audit")
public class AuditResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(AuditResource.class);

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private AuditService auditService;

    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public List<AuditResponseRo> users(AuditCriteria auditCriteria) {
        return domainConverter.convert(auditService.listAudit(auditCriteria.getAuditTargetName(),
                auditCriteria.getAction(),
                auditCriteria.getUser(),
                auditCriteria.getFrom(), auditCriteria.getTo(),
                auditCriteria.getStart(), auditCriteria.getMax()), AuditResponseRo.class);
    }
}
