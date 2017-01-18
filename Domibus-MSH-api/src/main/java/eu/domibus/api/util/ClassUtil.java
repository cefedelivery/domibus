package eu.domibus.api.util;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
public interface ClassUtil {

    String getTargetObjectClassCanonicalName(Object proxy) throws Exception;

    Class getTargetObjectClass(Object proxy) throws Exception;


}
