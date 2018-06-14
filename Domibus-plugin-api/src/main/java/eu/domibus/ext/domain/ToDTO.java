package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO class for To information details
 *
 * It stores information about To details
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class ToDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * {@link Set} of Party Ids {@link PartyIdDTO}
     */
    private Set<PartyIdDTO> partyId;

    /**
     * Role details {@link String}
     */
    private String role;

    /**
     * Gets the Set of Party Ids
     * @return {@link Set} of Party Ids {@link PartyIdDTO}
     */
    public Set<PartyIdDTO> getPartyId() {
        return partyId;
    }

    /**
     * Sets the Set of Party Ids
     * @param partyId {@link Set} of Party Ids {@link PartyIdDTO}
     */
    public void setPartyId(Set<PartyIdDTO> partyId) {
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
    public String toString() {
        return new ToStringBuilder(this)
                .append("partyId", partyId)
                .append("role", role)
                .toString();
    }
}
