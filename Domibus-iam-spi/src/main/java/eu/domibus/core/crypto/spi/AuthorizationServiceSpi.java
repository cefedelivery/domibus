package eu.domibus.core.crypto.spi;

import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.core.crypto.spi.model.UserMessagePmodeData;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;

import java.security.cert.X509Certificate;
import java.util.List;

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
     * @param userMessagePmodeData              a class containing information from domibus configuration.
     * @throws AuthorizationException if the message is not authorized.
     */
    void authorize(
            List<X509Certificate> signingCertificateTrustChain,
            X509Certificate signingCertificate,
            UserMessageDTO userMessageDTO,
            UserMessagePmodeData userMessagePmodeData) throws AuthorizationException;

    /**
     * Authorize a user message based its content, some metadata, the leaf certificate and its trust chain.
     *
     * @param signingCertificateTrustChain the signing certificate trust chain.
     * @param signingCertificate           the signing certificate.
     * @param pullRequestDTO               the PullRequest information.
     * @param pullRequestPmodeData           a class containing information from domibus configuration.
     * @throws AuthorizationException if the message is not authorized.
     */
    void authorize(List<X509Certificate> signingCertificateTrustChain,
                   X509Certificate signingCertificate,
                   PullRequestDTO pullRequestDTO,
                   PullRequestPmodeData pullRequestPmodeData) throws AuthorizationException;

    /**
     * If multiple instances of the AuthorizatoinServiceSpi are found,
     * the system will use the one that has identifier equal with the following property:
     * domibus.extension.iam.authorization.identifier
     *
     * @return the identifier of Authorization implementation.
     */
    String getIdentifier();
}
