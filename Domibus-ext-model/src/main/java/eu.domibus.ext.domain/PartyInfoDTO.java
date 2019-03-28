package eu.domibus.ext.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DTO class for the Party Info
 *
 * It stores some information about a party
 *
 * @author Tiago Miguel
 * @since 3.3
 */
public class PartyInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Party's From details {@link FromDTO}
     */
    private FromDTO from;

    /**
     * Party's To details {@link ToDTO}
     */
    private ToDTO to;

    /**
     * Gets Party's From details
     * @return Party's From details {@link FromDTO}
     */
    public FromDTO getFrom() {
        return from;
    }

    /**
     * Sets Party's From details
     * @param from Party's From details {@link FromDTO}
     */
    public void setFrom(FromDTO from) {
        this.from = from;
    }

    /**
     * Gets Party's To details
     * @return Party's To details {@link ToDTO}
     */
    public ToDTO getTo() {
        return to;
    }

    /**
     * Sets Party's To details
     * @param to Party's To details {@link ToDTO}
     */
    public void setTo(ToDTO to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("from", from)
                .append("to", to)
                .toString();
    }
}
