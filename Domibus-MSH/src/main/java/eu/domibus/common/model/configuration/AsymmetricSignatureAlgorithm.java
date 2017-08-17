
package eu.domibus.common.model.configuration;

import org.apache.wss4j.policy.SPConstants;

/**
 * @author Christian Koch, Stefan Muellern
 */
public enum AsymmetricSignatureAlgorithm {
    RSA_SHA1(SPConstants.RSA_SHA1), RSA_SHA256(SPConstants.RSA_SHA256);

    private final String algorithm;

    AsymmetricSignatureAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

}
