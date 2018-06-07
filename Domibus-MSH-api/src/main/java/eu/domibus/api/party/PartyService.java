package eu.domibus.api.party;

import eu.domibus.api.process.Process;
import eu.domibus.api.security.TrustStoreEntry;
import javafx.util.Pair;

import java.security.KeyStoreException;
import java.util.List;

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
     * Count parties for the given search criteria.
     * @param name criteria to search on the the name of the party
     * @param endPoint criteria to search on the endPoint of the party
     * @param partyId criteria to search within the partyids of the party.
     * @param processName criteria to search party that are configured as initiator or responder in a process named like this criteria
     * @return the number of party.
     */
    long countParties(String name, String endPoint, String partyId, String processName);

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
    void updateParties(List<Party> partyList, List<Pair<String, String>> certificateList);

    /**
     * Retrieve all the processes configured in the pmode.
     *
     * @return a lit of processes.
     */
    List<Process> getAllProcesses();

//    TrustStoreEntry getPartyCertificateFromTruststore(String partyName) throws KeyStoreException;
}
