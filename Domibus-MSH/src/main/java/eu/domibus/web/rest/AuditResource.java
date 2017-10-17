package eu.domibus.web.rest;

import eu.domibus.api.audit.AuditLog;
import eu.domibus.common.services.AuditService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.criteria.AuditCriteria;
import eu.domibus.web.rest.ro.AuditResponseRo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
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

    @RequestMapping(value = {"/list"}, method = RequestMethod.POST)
    public List<AuditResponseRo> listAudits(@RequestBody AuditCriteria auditCriteria) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Audit criteria received:");
            LOG.debug(auditCriteria.toString());
        }
        List<AuditLog> sourceList = auditService.listAudit(
                auditCriteria.getAuditTargetName(),
                auditCriteria.getUser(),
                auditCriteria.getAction(),
                auditCriteria.getFrom(),
                auditCriteria.getTo(),
                auditCriteria.getStart(),
                auditCriteria.getMax());

        return domainConverter.convert(sourceList, AuditResponseRo.class);
    }


    @RequestMapping(value = {"/count"}, method = RequestMethod.POST)
    public Long countAudits(@RequestBody AuditCriteria auditCriteria) {
        return auditService.countAudit(
                auditCriteria.getAuditTargetName(),
                auditCriteria.getUser(),
                auditCriteria.getAction(),
                auditCriteria.getFrom(),
                auditCriteria.getTo());
    }

    @RequestMapping(value = {"/targets"}, method = RequestMethod.GET)
    public List<String> auditTargets() {
        return auditService.listAuditTarget();
    }
}
