
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
