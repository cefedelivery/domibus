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

    public List<ConfigurationRaw> getLatest(){
        final TypedQuery<ConfigurationRaw> query = this.em.createNamedQuery("ConfigurationRaw.getLatest", ConfigurationRaw.class);
        query.setMaxResults(1);
        return query.getResultList();
    }

}
