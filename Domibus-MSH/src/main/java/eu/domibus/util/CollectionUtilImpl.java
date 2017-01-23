package eu.domibus.util;

import eu.domibus.api.util.CollectionUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@Component
public class CollectionUtilImpl implements CollectionUtil {

    @Override
    public <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex) {
        if(list == null) {
            return Collections.emptyList();
        }

        int size = list.size();
        if (fromIndex >= size || toIndex <= 0 || fromIndex >= toIndex) {
            return Collections.emptyList();
        }

        fromIndex = Math.max(0, fromIndex);
        toIndex = Math.min(size, toIndex);

        return list.subList(fromIndex, toIndex);
    }
}
