package eu.domibus.ext.delegate.converter;

import eu.domibus.api.acknowledge.MessageAcknowledgement;
import eu.domibus.ext.domain.MessageAcknowledgementDTO;

import java.util.List;

/**
 * Class responsible of conversion from the internal domain to external domain and the other way around
 *
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
public interface DomibusDomainConverter {

    <T, U> T convert(U source, Class<T> typeOfT);

    <T, U> List<T> convert(List<U> sourceList, Class<T> typeOfT);

}
