package eu.domibus.core.pull;

import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.Party;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 *
 * {@inheritDoc}
 *
 */
public class PartyExtractor implements PartyIdExtractor {

    private final static Logger LOG = LoggerFactory.getLogger(PartyExtractor.class);

    private final Party party;

    public PartyExtractor(final Party party) {
        this.party = party;
    }

    /**
     * @return the first identifier of the list. Throw IllegalStateException if none exists.
     */
    @Override
    public String getPartyId() {
        Set<Identifier> identifiers = party.getIdentifiers();
        if (identifiers.size() == 0) {
            LOG.warn("No identifier defined for party:[{}], the message will not be available for pulling", party.getName());
            throw new IllegalStateException("Party should have an identifier");
        }
        Identifier identifier = identifiers.iterator().next();
        return identifier.getPartyId();
    }
}
