package eu.domibus.core.party;

import org.springframework.expression.spel.ast.Identifier;

import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class PartyResponseRo {

    private Integer entityId;

    protected Set<Identifier> identifiers; //NOSONAR

    protected String name;

    protected String userName;

    protected String endpoint;

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
