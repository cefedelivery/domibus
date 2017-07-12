package eu.domibus.common.services;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.logging.RawEnvelopeDto;
import eu.domibus.common.services.impl.PullContext;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.UserMessage;

/**
 * @author Thomas Dussart
 * @since 3.3
 * Service returning information about the message exchange.
 */

public interface MessageExchangeService {

    /**
     * This method with analyse the messageExchange in order to find if the message should be pushed of pulled.
     * The status will be set in messageExchangeContext.
     * @param messageExchangeConfiguration the message configuration used to retrieve the associated process.
     * @return the status of the message.
     */
    MessageStatus getMessageStatus(final MessageExchangeConfiguration messageExchangeConfiguration);

    /**
     * Load pmode and find pull process in order to initialize pull request.
     */
    void initiatePullRequest();

    /**
     * Check if a message exist for the association mpc/responder. If it does it returns the first one that arrived.
     *
     * @param mpc       the mpc contained in the pull request.
     * @param responder the party for who this message is related.
     * @return a UserMessage if found.
     */
    UserMessage retrieveReadyToPullUserMessages(String mpc, Party responder);

    /**
     * When a pull request comes in, there is very litle information.  From this information we retrieve
     * the initiator, the responder and the pull process leg configuration from wich we can retrieve security information
     * @param mpcQualifiedName the mpc attribute within the pull request.
     * @return a pullcontext with all the information needed to continue with the pull process.
     */
    PullContext extractProcessOnMpc(String mpcQualifiedName);

    /**
     * In case of a pull message, the output soap envelope needs to be saved in order to be saved in order to check the
     * non repudiation.
     * @param rawXml the soap envelope
     * @param messageId the user message
     */
    void savePulledMessageRawXml(String rawXml, String messageId);


    RawEnvelopeDto findPulledMessageRawXmlByMessageId(String messageId);

    //@thom test this method.
    void removeRawMessageIssuedByPullRequest(String messageId);
}
