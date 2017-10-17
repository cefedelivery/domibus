package eu.domibus.web.rest.ro;

import java.util.Date;

public class PModeResponseRO {

    int id;
    Date configurationDate;
    String username;
    String description;
    boolean current;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public boolean isCurrent() {
        return current;
    }

    public void setCurrent(boolean current) {
        this.current = current;
    }
}
