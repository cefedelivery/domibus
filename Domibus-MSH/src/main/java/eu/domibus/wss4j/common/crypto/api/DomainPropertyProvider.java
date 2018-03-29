package eu.domibus.wss4j.common.crypto.api;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface DomainPropertyProvider {

    String getPropertyName(String domain, String propertyName);

    String getPropertyValue(String domain, String propertyName);

    String getResolvedPropertyValue(String domain, String propertyName);
}
