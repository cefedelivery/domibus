package eu.domibus.ebms3.common.context;

import eu.domibus.common.MessageStatus;

/**
 * Created by dussath on 5/18/17.
 * Class in charge of keeping track of the exchange information.
 */
public class MessageExchangeContext {

    private final String agreementName;
    private final String senderParty;
    private final String receiverParty;
    private final String service;
    private final String action;
    private final String leg;
    private final String pmodeKey;
    private final String reversePmodeKey;
    final static String SEPARATOR=":";
    private MessageStatus messageStatus;

    public MessageExchangeContext(final String agreementName, final String senderParty, final String receiverParty, final String service, final String action, final String leg) {
        this.agreementName = agreementName;
        this.senderParty = senderParty;
        this.receiverParty = receiverParty;
        this.service = service;
        this.action = action;
        this.leg = leg;
        this.pmodeKey=senderParty + SEPARATOR + receiverParty + SEPARATOR+ service + SEPARATOR+ action + SEPARATOR+ agreementName + SEPARATOR+ leg;
        this.reversePmodeKey=receiverParty+ SEPARATOR + senderParty+ SEPARATOR+ service + SEPARATOR+ action + SEPARATOR+ agreementName + SEPARATOR+ leg;
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

    public void updateStatus(MessageStatus messageStatus){
        this.messageStatus=messageStatus;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageExchangeContext that = (MessageExchangeContext) o;

        if (agreementName != null ? !agreementName.equals(that.agreementName) : that.agreementName != null)
            return false;
        if (!senderParty.equals(that.senderParty)) return false;
        if (!receiverParty.equals(that.receiverParty)) return false;
        if (!service.equals(that.service)) return false;
        if (!action.equals(that.action)) return false;
        if (!leg.equals(that.leg)) return false;
        return pmodeKey.equals(that.pmodeKey);
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
