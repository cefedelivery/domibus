package eu.domibus.core.converter;

import java.util.List;

/**
 * Class responsible of conversion of database entities to the core domain and the other way around
 *
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface DomainCoreConverter {

    <T, U> T convert(U source, Class<T> typeOfT);

    <T, U> List<T> convert(List<U> sourceList, Class<T> typeOfT);

}
