package eu.domibus.core.party;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Dussart
 * @since 4.0
 *
 * DTO class used to link party with process
 */
public class PartyProcessLinkRo {

    private int entity_id;

    private String processName;

    private boolean initiator;

    private boolean responder;

    public PartyProcessLinkRo(String processName, boolean initiator, boolean responder) {
        this.processName = processName;
        this.initiator = initiator;
        this.responder = responder;
    }

    public int getEntity_id() {
        return entity_id;
    }

    public void setEntity_id(int entity_id) {
        this.entity_id = entity_id;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public boolean isInitiator() {
        return initiator;
    }

    public void setInitiator(boolean initiator) {
        this.initiator = initiator;
    }

    public boolean isResponder() {
        return responder;
    }

    public void setResponder(boolean responder) {
        this.responder = responder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        PartyProcessLinkRo that = (PartyProcessLinkRo) o;

        return new EqualsBuilder()
                .append(initiator, that.initiator)
                .append(responder, that.responder)
                .append(processName, that.processName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(processName)
                .append(initiator)
                .append(responder)
                .toHashCode();
    }
}
