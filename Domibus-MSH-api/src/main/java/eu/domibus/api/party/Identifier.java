package eu.domibus.api.party;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class Identifier {

    private String partyId;

    private PartyIdType partyIdType;

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public PartyIdType getPartyIdType() {
        return partyIdType;
    }

    public void setPartyIdType(PartyIdType partyIdType) {
        this.partyIdType = partyIdType;
    }

    @Override
    public String toString() {
        return "Identifier{" + "partyId=" + partyId + ", partyIdType=" + partyIdType + '}';
    }

}
