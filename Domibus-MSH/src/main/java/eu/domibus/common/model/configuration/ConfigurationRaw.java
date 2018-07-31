
package eu.domibus.common.model.configuration;

import eu.domibus.common.model.common.RevisionLogicalName;
import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "TB_CONFIGURATION_RAW")
@NamedQueries({
        @NamedQuery(name = "ConfigurationRaw.getById",
                query = "select conf from ConfigurationRaw conf WHERE conf.entityId = :CONF_ID"),
        @NamedQuery(name = "ConfigurationRaw.getDetailedList",
        query = "select new eu.domibus.api.pmode.PModeArchiveInfo(c.entityId, c.configurationDate, r.userName, c.description) From ConfigurationRaw c, RevisionLog r join r.revisionTypes as ea where ea.entityId=c.entityId")
}
)
@Audited(withModifiedFlag = true)
@RevisionLogicalName(value = "Pmode Archive", auditOrder = 1)
public class ConfigurationRaw extends AbstractBaseEntity {

    @Lob
    @Column(name = "XML")
    @NotAudited
    byte[] xml;

    @Column(name = "CONFIGURATION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    Date configurationDate;

    @Column(name = "DESCRIPTION")
    String description;

    public byte[] getXml() {
        return xml;
    }

    public void setXml(byte[] xml) {
        this.xml = xml;
    }

    public Date getConfigurationDate() {
        return configurationDate;
    }

    public void setConfigurationDate(Date configurationDate) {
        this.configurationDate = configurationDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
