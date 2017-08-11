package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 3.3
 * Data acces for process entity.
 */
public interface ProcessDao {
    /**
     * Search for processes that correspond to the message exchange configuration.
     * @param messageExchangeConfiguration contains information about the exchange.
     * @return the corresponding processes.
     */
    List<Process> findPullProcessesByMessageContext(final MessageExchangeConfiguration messageExchangeConfiguration);

    /**
     * Retrieve Process with pull binding having party as an initiator.
     * @param party the initiator.
     * @return the matching processes.
     */
    List<Process> findPullProcessesInitiator(final Party party);


    /**
     * Returns a list of pullProcess based on an mpc.
     * @param mpc the message partition channel
     * @return the matching processes.
     */
    List<Process> findPullProcessBytMpc(String mpc);
}
