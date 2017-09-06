
package eu.domibus.plugin.discovery;

import eu.domibus.common.dao.PartyDao;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.model.configuration.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class PartyIndentifierResolverService {

    @Autowired
    private PartyDao partyDao;

    public Collection<Identifier> resolveByEndpoint(final String endpoint) throws ConfigurationException {
        if (endpoint == null || endpoint.isEmpty()) {
            throw new ConfigurationException("Endpoint empty or null");
        }

        final Collection<Identifier> listOfIdentifiers = partyDao.findPartyIdentifiersByEndpoint(endpoint);

        if (listOfIdentifiers.isEmpty()) {
            throw new ConfigurationException("Can not resolve identifiers for endpoint: " + endpoint);
        }

        return listOfIdentifiers;
    }
}
