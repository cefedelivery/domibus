package eu.domibus.security.authorization;

import java.security.cert.X509Certificate;

public interface AuthorizationService {

    /**
     * Check that the sender of a message has the right to submit an AS4 message to the system.
     *
     * @param sender data identifying the sender.
     * @param destination the MSH client to who this message is targeted.
     * @param signingCertificate the leaf certificate used to sign the AS4 message.
     * @param certificateTrustChain the chain of certificate  used
     */
    void authorize(
            Sender sender,
            Destination destination,
            X509Certificate signingCertificate,
            X509Certificate certificateTrustChain);
}
