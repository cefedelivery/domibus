package eu.domibus.core.pmode;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public abstract class AbstractProcessPartyExtractor implements ProcessTypePartyExtractor {

    protected final String senderParty;
    protected final String receiverParty;

    public AbstractProcessPartyExtractor(String senderParty, String receiverParty) {
        this.senderParty = senderParty;
        this.receiverParty = receiverParty;
    }

}
