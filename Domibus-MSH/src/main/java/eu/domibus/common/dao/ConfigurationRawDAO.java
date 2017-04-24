package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.ConfigurationRaw;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

@Repository
public class ConfigurationRawDAO extends BasicDao<ConfigurationRaw> {

    public ConfigurationRawDAO() {
        super(ConfigurationRaw.class);
    }

    public ConfigurationRaw getLatest(){
        final TypedQuery<ConfigurationRaw> query = this.em.createNamedQuery("ConfigurationRaw.getLatest", ConfigurationRaw.class);
        return query.getSingleResult();
    }

}
