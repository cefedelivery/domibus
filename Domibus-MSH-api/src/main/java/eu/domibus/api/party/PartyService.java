package eu.domibus.api.party;

import eu.domibus.api.process.Process;
import java.util.List;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface PartyService {

    /**
     * Search the parties configured in the pmode. The search is made base on the following criterias.
     *
     * @param name criteria to search on the the name of the party
     * @param endPoint criteria to search on the endPoint of the party
     * @param partyId criteria to search within the partyids of the party.
     * @param processName criteria to search party that are configured as initiator or responder in a process named like this criteria
     * @param pageStart pagination start
     * @param pageSize page size.
     * @return a lit of party.
     */
    List<Party> getParties(String name,
                           String endPoint,
                           String partyId,
                           String processName,
                           int pageStart,
                           int pageSize);

    /**
     * Returns the list of Party Names for a specific Service and Action
     * @param service Service name
     * @param action Action name
     * @return List of Party names
     */
    List<String> findPartyNamesByServiceAndAction(final String service, final String action);

    /**
     * Returns the Party Identifier Name for the gateway party
     * @return Party Identifier Name
     */
    String getGatewayPartyIdentifier();

    /**
     * Updates the list of parties.
     * @param partyList
     */
    void updateParties(List<Party> partyList, Map<String, String> certificates);

    /**
     * Retrieve all the processes configured in the pmode.
     *
     * @return a lit of processes.
     */
    List<Process> getAllProcesses();
}
