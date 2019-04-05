package eu.domibus.core.security;

import eu.domibus.core.crypto.spi.AuthorizationServiceSpi;
import eu.domibus.core.crypto.spi.PullRequestPmodeData;
import eu.domibus.core.crypto.spi.model.AuthorizationException;
import eu.domibus.core.crypto.spi.model.UserMessagePmodeData;
import eu.domibus.ext.domain.PullRequestDTO;
import eu.domibus.ext.domain.UserMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.1
 *
 * Default authorization implementation.
 * Still needs to be implemented.
 */
@Component
public class DefaultAuthorizationServiceSpiImpl implements AuthorizationServiceSpi {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAuthorizationServiceSpiImpl.class);

    protected static final String DEFAULT_IAM_AUTHORIZATION_IDENTIFIER = "DEFAULT_IAM_AUTHORIZATION_SPI";

    /**
     * {@inheritDoc}
     */
    @Override
    public void authorize(List<X509Certificate> signingCertificateTrustChain, X509Certificate signingCertificate, UserMessageDTO userMessageDTO, UserMessagePmodeData userMessagePmodeData) {
        LOG.info("Default authorization not implemented.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void authorize(List<X509Certificate> signingCertificateTrustChain, X509Certificate signingCertificate, PullRequestDTO pullRequestDTO, PullRequestPmodeData pullRequestPmodeData) throws AuthorizationException {
        LOG.info("Default authorization not implemented.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdentifier() {
        return DEFAULT_IAM_AUTHORIZATION_IDENTIFIER;
    }

}
