package eu.domibus.api.party;

import eu.domibus.api.party.Party;

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
}
