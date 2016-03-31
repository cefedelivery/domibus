/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

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
