package eu.domibus.ext.delegate.converter;

import org.dozer.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author migueti, Cosmin Baciu
 * @since 3.3
 */
@Component
public class DomibusDomainDefaultConverter implements DomibusDomainConverter {

    @Autowired
    Mapper mapper;

    @Override
    public <T, U> T convert(U source, final Class<T> typeOfT) {
        return mapper.map(source, typeOfT);
    }

    @Override
    public <T, U> List<T> convert(List<U> sourceList, final Class<T> typeOfT) {
        if (sourceList == null) {
            return null;
        }
        List<T> result = new ArrayList<>();
        for (U sourceObject : sourceList) {
            result.add(convert(sourceObject, typeOfT));

        }
        return result;
    }

}
