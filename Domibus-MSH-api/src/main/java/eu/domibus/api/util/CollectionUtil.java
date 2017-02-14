package eu.domibus.api.util;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public interface CollectionUtil {

    /**
     * Returns a view of the portion of this list between the specified <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.
     * In case the <tt>list</tt> is null or the condition <tt>fromIndex >= list.size() || toIndex <= 0 || fromIndex >= toIndex</tt> is not met it returns and empty list
     */
    <T> List<T> safeSubList(List<T> list, int fromIndex, int toIndex);
}
