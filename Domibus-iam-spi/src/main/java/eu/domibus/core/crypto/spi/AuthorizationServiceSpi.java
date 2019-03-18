package eu.domibus.core.crypto.spi;

import eu.domibus.core.crypto.spi.model.PullRequestMapping;
import eu.domibus.core.crypto.spi.model.UserMessageMapping;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;

import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface AuthorizationServiceSpi {

    boolean authorize(X509Certificate[] certs, UserMessageDTO userMessage, Map<UserMessageMapping, String> messageMappings);

    boolean authorize(X509Certificate[] certs, PullRequestDTO pullRequestDTO, Map<PullRequestMapping, String> pullRequestMapping);

    String getIdentifier();
}
