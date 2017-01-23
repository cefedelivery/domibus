package eu.domibus.api.util;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public interface CollectionUtil {

    <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex);
}
