package eu.domibus.common.services;

import eu.domibus.common.services.impl.PullContext;
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

    /**
     * Load pmode and find pull process in order to initialize pull request.
     */
    void initiatePullRequest();

    /**
     * When a pull request comes in, there is very litle information. Basicaly the mpc, and if there is
     * a certificate the possibility to retrieve the name of the sender from it. From this information we retrieve
     * the initiator, the responder and the pull process configuration. This will allow us to create a Pmode key and
     * link the request to a policiy.
     * @param initiatorName the access point that initiate the request.
     * @param mpcQualifiedName the mpc attribute within the pull request.
     * @return a pullcontext with all the information needed to continue with the pull process.
     */
    PullContext extractPullRequestProcessInformation(String initiatorName, String mpcQualifiedName);
}
