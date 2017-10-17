
package eu.domibus.common.model.configuration;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "TB_CONFIGURATION_RAW")
@NamedQueries({
        @NamedQuery(name = "ConfigurationRaw.getLatest",
                query = "select conf from ConfigurationRaw conf ORDER BY conf.configurationDate desc")
}
)
public class ConfigurationRaw extends AbstractBaseEntity {

    @Lob
    @Column(name = "XML")
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
