package eu.domibus.core.party;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface PartyService {

    List<Party> listParties(String name,
                            String endPoint,
                            String partyId,
                            String process,
                            int pargeStart,
                            int pageSize);
}
