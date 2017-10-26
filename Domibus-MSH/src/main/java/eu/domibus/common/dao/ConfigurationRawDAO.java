package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.ConfigurationRaw;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;

@Repository
public class ConfigurationRawDAO extends BasicDao<ConfigurationRaw> {

    public ConfigurationRawDAO() {
        super(ConfigurationRaw.class);
    }

    public ConfigurationRaw getLatest() {
        final TypedQuery<ConfigurationRaw> query = this.em.createNamedQuery("ConfigurationRaw.getLatest", ConfigurationRaw.class);
        query.setMaxResults(1);
        final List<ConfigurationRaw> resultList = query.getResultList();
        if (!resultList.isEmpty())
            return resultList.get(0);
        return null;
    }

    public List<ConfigurationRaw> getList() {
        final TypedQuery<ConfigurationRaw> query = this.em.createNamedQuery("ConfigurationRaw.getLatest", ConfigurationRaw.class);
        return query.getResultList();
    }

    public ConfigurationRaw getConfigurationRaw(int id) {
        final TypedQuery<ConfigurationRaw> query = this.em.createNamedQuery("ConfigurationRaw.getById", ConfigurationRaw.class);
        query.setParameter("CONF_ID", id);
        return query.getSingleResult();
    }

    public void deleteById(int id) {
        ConfigurationRaw configurationRaw = new ConfigurationRaw();
        configurationRaw.setEntityId(id);
        this.delete(configurationRaw);
    }
}
