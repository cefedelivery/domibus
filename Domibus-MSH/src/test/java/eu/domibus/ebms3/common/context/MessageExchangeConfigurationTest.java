package eu.domibus.ebms3.common.context;

import org.junit.Test;

import static eu.domibus.ebms3.common.context.MessageExchangeConfiguration.SEPARATOR;
import static org.junit.Assert.assertEquals;
/**
 * @author Thomas Dussart
 * @since 3.3
 *
 */
public class MessageExchangeConfigurationTest {

    @Test
    public void testMessageExchange(){
        //final String agreementName, final String senderParty, final String receiverParty, final String service, final String action, final String leg,String pmodeKey) {
        String agreementName="agreementName";
        String senderParty="senderParty";
        String receiverParty="receiverParty";
        String service="service";
        String action="action";
        String leg="leg";

        MessageExchangeConfiguration messageExchangeConfiguration = new MessageExchangeConfiguration(agreementName, senderParty, receiverParty, service, action, leg);
        assertEquals(agreementName, messageExchangeConfiguration.getAgreementName());
        assertEquals(senderParty, messageExchangeConfiguration.getSenderParty());
        assertEquals(receiverParty, messageExchangeConfiguration.getReceiverParty());
        assertEquals(service, messageExchangeConfiguration.getService());
        assertEquals(action, messageExchangeConfiguration.getAction());
        assertEquals(leg, messageExchangeConfiguration.getLeg());
        assertEquals(senderParty+SEPARATOR+receiverParty+SEPARATOR+service+SEPARATOR+action+SEPARATOR+agreementName+SEPARATOR+leg, messageExchangeConfiguration.getPmodeKey());
    }

}