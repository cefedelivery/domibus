package eu.domibus.web.rest.ro;

import java.util.Date;

public class PModeResponseRO {

    Date configurationDate;
    String username;
    String description;

    public Date getConfigurationDate() {
        return configurationDate;
    }

    public void setConfigurationDate(Date configurationDate) {
        this.configurationDate = configurationDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
