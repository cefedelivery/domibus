package eu.domibus.common.dao;

import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.common.model.common.RevisionLog;
import eu.domibus.common.model.configuration.ConfigurationRaw;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ConfigurationRawDAO extends BasicDao<ConfigurationRaw> {

    public ConfigurationRawDAO() {
        super(ConfigurationRaw.class);
    }

    public ConfigurationRaw getConfigurationRaw(int id) {
        final TypedQuery<ConfigurationRaw> query = this.em.createNamedQuery("ConfigurationRaw.getById", ConfigurationRaw.class);
        query.setParameter("CONF_ID", id);
        return query.getSingleResult();
    }

    public List<PModeArchiveInfo> getDetailedConfigurationRaw() {
        AuditReader auditReader = AuditReaderFactory.get(em);
        //load Configuration raw + audit, skiping the deleted raws.
        AuditQuery auditQuery = auditReader.createQuery().forRevisionsOfEntity(ConfigurationRaw.class, false, false);
        //retrive only the last revision or each entity.
        auditQuery.add(AuditEntity.revisionNumber().maximize().computeAggregationInInstanceContext());
        //sort by revision desc.
        auditQuery.addOrder(AuditEntity.revisionNumber().desc());
        List<Object[]> resultList = auditQuery.getResultList();
        return resultList.stream().map(o -> {
            ConfigurationRaw configurationRaw = (ConfigurationRaw) o[0];
            RevisionLog revisionLog = (RevisionLog) o[1];
            return new PModeArchiveInfo(
                    configurationRaw.getEntityId(),
                    configurationRaw.getConfigurationDate(),
                    revisionLog.getUserName(),
                    configurationRaw.getDescription());
        }).collect(Collectors.toList());
    }

    public void deleteById(int id) {
        final ConfigurationRaw configurationRaw = read(id);
        this.delete(configurationRaw);
    }
}
