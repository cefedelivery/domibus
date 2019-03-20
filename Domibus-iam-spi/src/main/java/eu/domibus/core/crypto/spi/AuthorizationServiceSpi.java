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
 * <p>
 * Custom authorization implementation should implement this class.
 */
public interface AuthorizationServiceSpi {

    /**
     * Authorize a user message based its content, some metadata, the leaf certificate and its trust chaing.
     *
     * @param signingCertificateTrustChain the signing certificate trust chain.
     * @param signingCertificate           the signing certificate
     * @param userMessageDTO               the UserMessage information.
     * @param messageMappings              a map containing information from domibus configuration.
     * @throws AuthorizationException if the message is not authorized.
     */
    void authorize(
            List<X509Certificate> signingCertificateTrustChain,
            X509Certificate signingCertificate,
            UserMessageDTO userMessageDTO,
            Map<UserMessageMapping, String> messageMappings) throws AuthorizationException;

    /**
     * Authorize a user message based its content, some metadata, the leaf certificate and its trust chain.
     *
     * @param signingCertificateTrustChain the signing certificate trust chain.
     * @param signingCertificate           the signing certificate.
     * @param pullRequestDTO               the PullRequest information.
     * @param pullRequestMapping           a map containing information from domibus configuration.
     * @throws AuthorizationException if the message is not authorized.
     */
    void authorize(List<X509Certificate> signingCertificateTrustChain,
                   X509Certificate signingCertificate,
                   PullRequestDTO pullRequestDTO,
                   Map<PullRequestMapping, String> pullRequestMapping) throws AuthorizationException;

    /**
     * If multiple instances of the AuthorizatoinServiceSpi are found,
     * the system will use the one that has identifier equal with the following property:
     * domibus.extension.iam.authorization.identifier
     *
     * @return the identifier of Authorization implementation.
     */
    String getIdentifier();
}
