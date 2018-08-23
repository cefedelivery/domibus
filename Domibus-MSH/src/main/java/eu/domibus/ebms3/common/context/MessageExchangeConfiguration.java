package eu.domibus.ebms3.common.context;

/**
 * @author Thomas Dussart
 * @since 3.3
 * Class in charge of keeping track of the exchange information.
 */
public class MessageExchangeConfiguration {

    private final String agreementName;
    private final String senderParty;
    private final String receiverParty;
    private final String service;
    private final String action;
    private final String leg;
    private final String pmodeKey;
    private final String reversePmodeKey;
    public final static String PMODEKEY_SEPARATOR="_pMK_SEP_";

    public MessageExchangeConfiguration(final String agreementName, final String senderParty, final String receiverParty, final String service, final String action, final String leg) {
        this.agreementName = agreementName;
        this.senderParty = senderParty;
        this.receiverParty = receiverParty;
        this.service = service;
        this.action = action;
        this.leg = leg;
        this.pmodeKey=senderParty + PMODEKEY_SEPARATOR + receiverParty + PMODEKEY_SEPARATOR+ service + PMODEKEY_SEPARATOR+ action + PMODEKEY_SEPARATOR+ agreementName + PMODEKEY_SEPARATOR+ leg;
        this.reversePmodeKey=receiverParty+ PMODEKEY_SEPARATOR + senderParty+ PMODEKEY_SEPARATOR+ service + PMODEKEY_SEPARATOR+ action + PMODEKEY_SEPARATOR+ agreementName + PMODEKEY_SEPARATOR+ leg;
    }

    public String getAgreementName() {
        return agreementName;
    }

    public String getSenderParty() {
        return senderParty;
    }

    public String getReceiverParty() {
        return receiverParty;
    }

    public String getService() {
        return service;
    }

    public String getAction() {
        return action;
    }

    public String getLeg() {
        return leg;
    }

    public String getPmodeKey() {
        return pmodeKey;
    }

    public String getReversePmodeKey() {
        return reversePmodeKey;
    }

    @Override
    public String toString() {
        return "MessageExchangeConfiguration{" +
                "agreementName='" + agreementName + '\'' +
                ", senderParty='" + senderParty + '\'' +
                ", receiverParty='" + receiverParty + '\'' +
                ", service='" + service + '\'' +
                ", action='" + action + '\'' +
                ", leg='" + leg + '\'' +
                ", pmodeKey='" + pmodeKey + '\'' +
                ", reversePmodeKey='" + reversePmodeKey + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageExchangeConfiguration that = (MessageExchangeConfiguration) o;

        if (agreementName != null ? !agreementName.equalsIgnoreCase(that.agreementName) : that.agreementName != null)
            return false;
        if (!senderParty.equalsIgnoreCase(that.senderParty)) return false;
        if (!receiverParty.equalsIgnoreCase(that.receiverParty)) return false;
        if (!service.equalsIgnoreCase(that.service)) return false;
        if (!action.equalsIgnoreCase(that.action)) return false;
        if (!leg.equalsIgnoreCase(that.leg)) return false;
        return pmodeKey.equalsIgnoreCase(that.pmodeKey);
    }

    @Override
    public int hashCode() {
        int result = agreementName != null ? agreementName.hashCode() : 0;
        result = 31 * result + senderParty.hashCode();
        result = 31 * result + receiverParty.hashCode();
        result = 31 * result + service.hashCode();
        result = 31 * result + action.hashCode();
        result = 31 * result + leg.hashCode();
        result = 31 * result + pmodeKey.hashCode();
        return result;
    }
}
