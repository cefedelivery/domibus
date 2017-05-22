package eu.domibus.common.services;

import eu.domibus.ebms3.common.context.MessageExchangeContext;

/**
 * Created by dussath on 5/19/17.
 * Service returning information about the message exchange.
 */

public interface MessageExchangeService {

    /**
     * This method with analyse the messageExchange in order to find if the message should be pushed of pulled.
     * The status will be set in messsageExchangeContext.
     * @param messageExchangeContext the context of the messae.
     */
    void upgradeMessageExchangeStatus(final MessageExchangeContext messageExchangeContext);

}
