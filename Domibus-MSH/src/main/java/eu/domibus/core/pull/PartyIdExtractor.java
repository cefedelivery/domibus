package eu.domibus.core.pull;


/**
 * @author Thomas Dussart
 * @since 3.3.4
 *
 * The party concept has different representation in the model.
 * All those representation have the notion of identifier. This interface is an abstraction
 * helping the extraction of the identifier.
 *
 * @see eu.domibus.common.model.configuration.Party
 * @see eu.domibus.ebms3.common.model.To
 * @see eu.domibus.ebms3.common.model.From
 */
public interface PartyIdExtractor {

    /**
     * Return the identifier of the party.
     * @return
     */
    String getPartyId();

}
