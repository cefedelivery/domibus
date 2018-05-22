package eu.domibus.api.usermessage.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Tiago Miguel
 * @since 3.3.1
 */
public class PartyInfo {

    /**
     * Party's From details {@link From}
     */
    protected From from;

    /**
     * Party's To details {@link To}
     */
    protected To to;

    /**
     * Gets Party's From details
     * @return Party's From details {@link From}
     */
    public From getFrom() {
        return from;
    }

    /**
     * Sets Party's From details
     * @param from Party's From details {@link From}
     */
    public void setFrom(From from) {
        this.from = from;
    }

    /**
     * Gets Party's To details
     * @return Party's To details {@link To}
     */
    public To getTo() {
        return to;
    }

    /**
     * Sets Party's To details
     * @param to Party's To details {@link To}
     */
    public void setTo(To to) {
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PartyInfo partyInfo = (PartyInfo) o;

        return new EqualsBuilder()
                .append(from, partyInfo.from)
                .append(to, partyInfo.to)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(from)
                .append(to)
                .toHashCode();
    }
}
