package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.jaxb.simplecertificatereport.SimpleCertificateReport;
import eu.europa.esig.dss.jaxb.simplecertificatereport.XmlTrustAnchor;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.1
 * <p>
 * Dss validation produces Simple report element.
 * This class validate that all the certificate marked as anchor have and indication 'PASSED', which means
 * that at least the root CA is present in a trusted list.
 */
@Component
@Order(10)
public class SimpleReportTrustAnchorValidatorStep implements SimpleReportValidationStep {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleReportTrustAnchorValidatorStep.class);

    /**
     * The method searches for a chain element with a trust anchor and a passed indication.
     *
     * @param simpleCertificateReport the simple report provided by dss.
     * @return if the report is valid of not.
     */
    public boolean isValid(SimpleCertificateReport simpleCertificateReport) {
        //check if at least one element has a trust anchor.
        LOG.debug("Simple report:[{}]", simpleCertificateReport.toString());
        final boolean isThereAtLeastOneChainWitAnchor = simpleCertificateReport.
                getChain().
                stream().
                anyMatch(xmlChainItem -> xmlChainItem.getTrustAnchors() != null && !xmlChainItem.getTrustAnchors().isEmpty());
        if (!isThereAtLeastOneChainWitAnchor) return false;

        //filter based on trusted CA that would have an indication !PASSED.
        return simpleCertificateReport.
                getChain().
                stream().
                filter(xmlChainItem -> xmlChainItem.getTrustAnchors() != null && !xmlChainItem.getTrustAnchors().isEmpty()).
                filter(xmlChainItem -> xmlChainItem.getIndication() != null).
                anyMatch(trustedCa -> {
                    if (LOG.isDebugEnabled()) {
                        final List<XmlTrustAnchor> trustAnchors = trustedCa.getTrustAnchors();
                        for (XmlTrustAnchor trustAnchor : trustAnchors) {
                            LOG.debug("Anchor country code:[{}]", trustAnchor.getCountryCode());
                            LOG.debug("Anchor service name:[{}]", trustAnchor.getTrustServiceName());
                            LOG.debug("Anchor service provider:[{}]", trustAnchor.getTrustServiceProvider());
                        }
                    }
                    LOG.debug("Indication:[{}]", trustedCa.getIndication());
                    return Indication.PASSED.equals(trustedCa.getIndication());
                });
    }
}
