package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.jaxb.simplecertificatereport.SimpleCertificateReport;
import eu.europa.esig.dss.jaxb.simplecertificatereport.XmlTrustAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Dss validation produces Simple report element.
 * This class validate that all the certificate marked as anchor have and indication 'PASSED', which means
 * that at least the root CA is present in a trusted list.
 */
@Component
public class SimpleReportTrustAnchorValidatorStep implements SimpleReportValidationStep {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleReportTrustAnchorValidatorStep.class);

    public static final String PASSED = "PASSED";

    /**
     * Filter all the chain xml items from the simple report that have a trust anchor element.
     * The filtered list is filtered again base on the indication element which should not
     * be different than passed.
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
                anyMatch(xmlChainItem -> {
                    return xmlChainItem.getTrustAnchors() != null && !xmlChainItem.getTrustAnchors().isEmpty();

                });
        if (!isThereAtLeastOneChainWitAnchor) return false;

        //filter based on trusted CA that would have an indication !PASSED.
        return simpleCertificateReport.
                getChain().
                stream().
                filter(xmlChainItem -> xmlChainItem.getTrustAnchors() != null && !xmlChainItem.getTrustAnchors().isEmpty()).
                filter(trustedCa -> {
                    if (LOG.isDebugEnabled()) {
                        final List<XmlTrustAnchor> trustAnchors = trustedCa.getTrustAnchors();
                        for (XmlTrustAnchor trustAnchor : trustAnchors) {
                            LOG.debug("Anchor:[{}]", trustAnchor.getCountryCode());
                            LOG.debug("Anchor:[{}]", trustAnchor.getTrustServiceName());
                            LOG.debug("Anchor:[{}]", trustAnchor.getTrustServiceProvider());
                        }
                    }
                    LOG.debug("Indication:[{}]", trustedCa.getIndication().toString());
                    return !PASSED.equals(trustedCa.getIndication().toString());
                }).
                collect(Collectors.toList()).isEmpty();
    }
}
