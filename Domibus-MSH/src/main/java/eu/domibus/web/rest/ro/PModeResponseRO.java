package eu.domibus.web.rest.ro;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

public class PModeResponseRO {

    private int id;
    private Date configurationDate;
    private String username;
    private String description;
    private boolean current;

    public PModeResponseRO() {
    }

    public PModeResponseRO(int id, Date configurationDate, String username, String description) {
        setId(id);
        setConfigurationDate(configurationDate);
        setUsername(username);
        setDescription(description);
    }

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

    /*public String toCsvString() {
        return new StringBuilder()
                .append(Objects.toString(configurationDate,"")).append(",")
                .append(Objects.toString(username,"")).append(",")
                .append(Objects.toString(description,"")).append(",")
                .append(Objects.toString(current,""))
                .append(System.lineSeparator())
                .toString();
    }

    public static String csvTitle() {
        return new StringBuilder()
                .append("Configuration Date").append(",")
                .append("Username").append(",")
                .append("Description").append(",")
                .append("Current")
                .append(System.lineSeparator())
                .toString();
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PModeResponseRO that = (PModeResponseRO) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("configurationDate", configurationDate)
                .append("username", username)
                .append("description", description)
                .append("current", current)
                .toString();
    }
}
