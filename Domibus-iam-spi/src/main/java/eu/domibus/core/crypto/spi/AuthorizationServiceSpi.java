package eu.domibus.core.crypto.spi;

import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.core.crypto.spi.model.PullRequestMapping;
import eu.domibus.core.crypto.spi.model.UserMessageMapping;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public interface AuthorizationServiceSpi {

    boolean authorize(
            List<X509Certificate> signingCertificateTrustChain,
            X509Certificate signingCertificate,
            UserMessageDTO userMessage,
            Map<UserMessageMapping, String> messageMappings) throws AuthorizationException;

    boolean authorize(List<X509Certificate> signingCertificateTrustChain,
                      X509Certificate signingCertificate,
                      PullRequestDTO pullRequestDTO,
                      Map<PullRequestMapping, String> pullRequestMapping) throws AuthorizationException;

    String getIdentifier();
}
