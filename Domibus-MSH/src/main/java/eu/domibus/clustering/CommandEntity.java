package eu.domibus.clustering;

import eu.domibus.ebms3.common.model.AbstractBaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

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
}
