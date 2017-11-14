package eu.domibus.api.message.usermessage;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Set;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class To {

    protected Set<PartyId> partyId;

    protected String role;

    public Set<PartyId> getPartyId() {
        return partyId;
    }

    public void setPartyId(Set<PartyId> partyId) {
        this.partyId = partyId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        To to = (To) o;

        return new EqualsBuilder()
                .append(partyId, to.partyId)
                .append(role, to.role)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(partyId)
                .append(role)
                .toHashCode();
    }
}
