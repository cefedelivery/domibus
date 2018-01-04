package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Set;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class From {

    /**
     * {@link Set} of Party Ids {@link PartyId}
     */
    protected Set<PartyId> partyId;

    /**
     * Role details {@link String}
     */
    protected String role;

    /**
     * Gets the Set of Party Ids
     * @return {@link Set} of Party Ids {@link PartyId}
     */
    public Set<PartyId> getPartyId() {
        return partyId;
    }

    /**
     * Sets the Set of Party Ids
     * @param partyId {@link Set} of Party Ids {@link PartyId}
     */
    public void setPartyId(Set<PartyId> partyId) {
        this.partyId = partyId;
    }

    /**
     * Gets the Role details
     * @return Role details {@link String}
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the Role details
     * @param role Role details {@link String}
     */
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
