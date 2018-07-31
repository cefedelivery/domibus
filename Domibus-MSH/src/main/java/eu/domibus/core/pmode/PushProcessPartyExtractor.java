package eu.domibus.core.pmode;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

public class PushProcessPartyExtractor extends AbstractProcessPartyExtractor implements ProcessTypePartyExtractor {

    public PushProcessPartyExtractor(final String senderParty, final String receiverParty) {
        super(senderParty, receiverParty);
    }

    @Override
    public String getSenderParty() {
        return senderParty;
    }

    @Override
    public String getReceiverParty() {
        return receiverParty;
    }
}
