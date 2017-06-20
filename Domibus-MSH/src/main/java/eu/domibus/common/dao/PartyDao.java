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

package eu.domibus.common.dao;

import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Collection;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Repository
public class PartyDao extends BasicDao<Party> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyDao.class);
    public PartyDao() {
        super(Party.class);
    }

    public Collection<Identifier> findPartyIdentifiersByEndpoint(final String endpoint) {
        final Query query = this.em.createNamedQuery("Party.findPartyIdentifiersByEndpoint");
        query.setParameter("ENDPOINT", endpoint);

        return query.getResultList();
    }

}
