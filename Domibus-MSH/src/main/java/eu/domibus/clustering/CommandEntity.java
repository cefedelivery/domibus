package eu.domibus.clustering;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@Entity
@Table(name = "TB_COMMAND")
@NamedQueries({
        @NamedQuery(name = "CommandEntity.findByServerName", query = "SELECT c FROM CommandEntity c where c.serverName=:SERVER_NAME")
})
public class CommandEntity extends AbstractBaseEntity {

    @Column(name = "COMMAND_NAME")
    @NotNull
    protected String commandName;

    @Column(name = "SERVER_NAME")
    @NotNull
    protected String serverName;

    @Column(name = "DOMAIN")
    @NotNull
    protected String domain;

    @Column(name = "CREATION_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    protected Date creationTime;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "TB_COMMAND_PROPERTY", joinColumns = @JoinColumn(name =     "FK_COMMAND"))
    @MapKeyColumn(name = "PROPERTY_NAME", length = 50)
    @Column(name = "PROPERTY_VALUE", length = 100)
    @BatchSize(size = 20)
    private Map<String, String> commandProperties = new HashMap<>();

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Map<String, String> getCommandProperties() {
        return commandProperties;
    }

    public void setCommandProperties(Map<String, String> commandProperties) {
        this.commandProperties = commandProperties;
    }
}
