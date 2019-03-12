package eu.domibus.core.crypto.spi;

import eu.domibus.core.crypto.spi.model.UserMessage;

import java.security.cert.X509Certificate;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface AuthorizationServiceSpi {

    boolean authorize(X509Certificate[] certs, UserMessage userMessage);

    String getIdentifier();
}
