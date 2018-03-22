package eu.domibus.core.pull;

import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.common.model.To;
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
public class ToExtractor implements PartyIdExtractor {

    private final static Logger LOG = LoggerFactory.getLogger(ToExtractor.class);

    private To to;

    public ToExtractor(To to) {
        this.to = to;
    }

    /**
     * @return the first identifier of the list. Throw IllegalStateException if none exists.
     */
    @Override
    public String getPartyId() {
        Set<PartyId> identifiers = to.getPartyId();
        if (identifiers.size() == 0) {
            LOG.warn("No identifier defined for party with role:[{}], the message will not be available for pulling", to.getRole());
            throw new IllegalStateException("Party should have an identifier");
        }
        PartyId identifier = identifiers.iterator().next();
        return identifier.getValue();
    }
}
