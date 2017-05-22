package eu.domibus.ebms3.common.context;

import org.junit.Test;

import static eu.domibus.ebms3.common.context.MessageExchangeContext.SEPARATOR;
import static org.junit.Assert.assertEquals;
/**
 * Created by dussath on 5/18/17.
 *
 */
public class MessageExchangeContextTest {

    @Test
    public void testMessageExchange(){
        //final String agreementName, final String senderParty, final String receiverParty, final String service, final String action, final String leg,String pmodeKey) {
        String agreementName="agreementName";
        String senderParty="senderParty";
        String receiverParty="receiverParty";
        String service="service";
        String action="action";
        String leg="leg";

        MessageExchangeContext messageExchangeContext = new MessageExchangeContext(agreementName, senderParty, receiverParty, service, action, leg);
        assertEquals(agreementName,messageExchangeContext.getAgreementName());
        assertEquals(senderParty,messageExchangeContext.getSenderParty());
        assertEquals(receiverParty,messageExchangeContext.getReceiverParty());
        assertEquals(service,messageExchangeContext.getService());
        assertEquals(action,messageExchangeContext.getAction());
        assertEquals(leg,messageExchangeContext.getLeg());
        assertEquals(senderParty+SEPARATOR+receiverParty+SEPARATOR+service+SEPARATOR+action+SEPARATOR+agreementName+SEPARATOR+leg,messageExchangeContext.getPmodeKey());
    }

}