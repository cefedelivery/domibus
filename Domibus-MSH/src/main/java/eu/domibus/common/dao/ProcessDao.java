package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;

import java.util.List;

/**
 * Created by dussath on 5/18/17.
 * Data acces for process entity.
 */
public interface ProcessDao {
    /**
     * Search for processes that correspond to the message exchange configuration.
     * @param messageExchangeConfiguration contains information about the exchange.
     * @return the corresponding processes.
     */
    List<Process> findProcessByMessageContext(final MessageExchangeConfiguration messageExchangeConfiguration);

    /**
     * Retrieve Process with pull binding having party as an initiator.
     * @param party the initiator.
     * @return the matching processes.
     */
    List<Process> findPullProcessesByResponder(final Party party);

    /**
     * Returns a list of pullProcess based on requester name and mpc.
     * @param initiator the requester name.
     * @param mpc the message partition channeL
     * @return the matching processes.
     */
    List<Process> findPullProcessByRequestPartyAndMpc(String initiator, String mpc);

    /**
     * Returns a list of pullProcess based on an mpc.
     * @param mpc the message partition channeL
     * @return the matching processes.
     */
    List<Process> findPullProcessBytMpc(String mpc);
}
