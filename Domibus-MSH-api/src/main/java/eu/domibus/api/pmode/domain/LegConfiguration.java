package eu.domibus.api.pmode.domain;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class LegConfiguration {

    //TODO expose more details from the LegConfiguration entity in this class

    protected ReceptionAwareness receptionAwareness;

    public ReceptionAwareness getReceptionAwareness() {
        return receptionAwareness;
    }

    public void setReceptionAwareness(ReceptionAwareness receptionAwareness) {
        this.receptionAwareness = receptionAwareness;
    }
}
