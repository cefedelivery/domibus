package eu.domibus.core.crypto.spi;

import eu.domibus.core.crypto.spi.model.PullRequest;
import eu.domibus.core.crypto.spi.model.PullRequestMapping;
import eu.domibus.core.crypto.spi.model.UserMessage;
import eu.domibus.core.crypto.spi.model.UserMessageMapping;

import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public interface AuthorizationServiceSpi {

    boolean authorize(X509Certificate[] certs, UserMessage userMessage, Map<UserMessageMapping, String> messageMappings);

    boolean authorize(X509Certificate[] certs, PullRequest pullRequest, Map<PullRequestMapping, String> pullRequestMapping);

    String getIdentifier();
}
