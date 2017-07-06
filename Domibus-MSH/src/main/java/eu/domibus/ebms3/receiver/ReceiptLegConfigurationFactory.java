package eu.domibus.ebms3.receiver;

import eu.domibus.ebms3.common.model.Messaging;
import org.apache.cxf.binding.soap.SoapMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Dussart
 * @since 3.3
 */

@Component
@Qualifier("pullRequestLegConfigurationFactory")
public class ReceiptLegConfigurationFactory extends AbstractMessageLegConfigurationFactory {

    @Override
    protected LegConfigurationExtractor getConfiguration(SoapMessage soapMessage, Messaging messaging) {
        ReceiptLegConfigurationExtractor receiptLegConfigurationExtractor=null;
        if (messaging.getSignalMessage().getReceipt() != null) {
            receiptLegConfigurationExtractor = new ReceiptLegConfigurationExtractor(soapMessage, messaging);
        }
        return receiptLegConfigurationExtractor;
    }
}
