package eu.domibus.common.dao;

import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.common.model.configuration.ConfigurationRaw;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        List<PModeArchiveInfo> result = new ArrayList<>();
        //TODO: migueti: investigate in more detail envers to remove this native query and use an entity query
        final javax.persistence.Query query = this.em.createNativeQuery("SELECT tb_raw.ID_PK, tb_raw.CONFIGURATION_DATE, tb_info.USER_NAME, tb_raw.DESCRIPTION \n" +
                                                                                "FROM tb_configuration_raw tb_raw left join \n" +
                                                                                "(tb_configuration_raw_aud tb_raw_aud inner join \n" +
                                                                                "tb_rev_info tb_info on tb_raw_aud.REV = tb_info.ID and tb_raw_aud.REV = tb_info.ID and tb_raw_aud.REVTYPE = 0) " +
                                                                                "on tb_raw.ID_PK = tb_raw_aud.ID_PK " +
                                                                                " ORDER BY tb_raw.CONFIGURATION_DATE desc");
        final List<Object[]> list = query.getResultList();
        for(Object[] entry : list) {
            PModeArchiveInfo pModeArchiveInfo = new PModeArchiveInfo((Integer)entry[0], (Date)entry[1], (String)entry[2], (String)entry[3]);
            result.add(pModeArchiveInfo);
        }
        return result;
    }

    public void deleteById(int id) {
        final ConfigurationRaw configurationRaw = read(id);
        this.delete(configurationRaw);
    }
}
