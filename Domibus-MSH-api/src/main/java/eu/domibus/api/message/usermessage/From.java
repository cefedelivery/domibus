package eu.domibus.api.message.usermessage;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.Set;

public class From {

    private Set<PartyId> partyId;

    private String role;

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

        From from = (From) o;

        return new EqualsBuilder()
                .append(partyId, from.partyId)
                .append(role, from.role)
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
