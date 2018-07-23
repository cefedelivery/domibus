package eu.domibus.core.pmode;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class PullProcessPartyExtractor extends AbstractProcessPartyExtractor {

    public PullProcessPartyExtractor(String senderParty, String receiverParty) {
        super(senderParty, receiverParty);
    }

    @Override
    public String getSenderParty() {
        return receiverParty;
    }

    @Override
    public String getReceiverParty() {
        return senderParty;
    }
}
