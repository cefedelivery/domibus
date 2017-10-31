package eu.domibus.core.party;

import org.apache.commons.lang.StringUtils;

import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class PartyResponseRo {

    private Integer entityId;

    protected Set<IdentifierRo> identifiers; //NOSONAR

    protected String name;

    protected String userName;

    protected String endpoint;

    public Set<IdentifierRo> getIdentifiers() {
        return identifiers;
    }

    public String flatIdentifiers;

    public void setIdentifiers(Set<IdentifierRo> identifiers) {
        this.identifiers = identifiers;
        flatIdentifiers = StringUtils.join(identifiers, ",");
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

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public String getFlatIdentifiers() {
        return flatIdentifiers;
    }

    public void setFlatIdentifiers(String flatIdentifiers) {
        this.flatIdentifiers = flatIdentifiers;
    }
}
