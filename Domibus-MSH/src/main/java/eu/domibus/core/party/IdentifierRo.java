package eu.domibus.core.party;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class IdentifierRo {


    private String partyId;

    private PartyIdTypeRo partyIdType;

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public PartyIdTypeRo getPartyIdType() {
        return partyIdType;
    }

    public void setPartyIdType(PartyIdTypeRo partyIdType) {
        this.partyIdType = partyIdType;
    }
}
