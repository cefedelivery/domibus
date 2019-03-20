package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.jaxb.simplecertificatereport.SimpleCertificateReport;
import eu.europa.esig.dss.jaxb.simplecertificatereport.XmlChainItem;
import eu.europa.esig.dss.jaxb.simplecertificatereport.XmlTrustAnchor;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
@RunWith(JMockit.class)
public class SimpleReportTrustAnchorValidatorStepTest {

    @Tested
    private SimpleReportTrustAnchorValidatorStep simpleReportTrustAnchorValidatorStep;

    @Test
    public void isValidNoTrustNullAnchor(@Mocked final SimpleCertificateReport simpleCertificateReport,
                                         @Mocked final XmlChainItem chainItem) {
        List<XmlChainItem> chains = new ArrayList<>();
        chains.add(chainItem);

        new Expectations() {{
            simpleCertificateReport.getChain();
            result = chains;

            chainItem.getTrustAnchors();
            result = null;

        }};

        Assert.assertFalse(simpleReportTrustAnchorValidatorStep.isValid(simpleCertificateReport));
    }


    @Test
    public void isValidNoTrustEmptyAnchor(@Mocked final SimpleCertificateReport simpleCertificateReport,
                                          @Mocked final XmlChainItem chainItem) {
        List<XmlChainItem> chains = new ArrayList<>();
        chains.add(chainItem);

        new Expectations() {{
            simpleCertificateReport.getChain();
            result = chains;

            chainItem.getTrustAnchors();
            result = new ArrayList<>();

        }};

        Assert.assertFalse(simpleReportTrustAnchorValidatorStep.isValid(simpleCertificateReport));
    }

    @Test
    public void isValidWithAnchorButIndicationIsNull(@Mocked final SimpleCertificateReport simpleCertificateReport,
                                                     @Mocked final XmlChainItem chainItem,
                                                     @Mocked final XmlTrustAnchor xmlTrustAnchor) {
        List<XmlChainItem> chains = new ArrayList<>();
        chains.add(chainItem);

        List<XmlTrustAnchor> anchors = new ArrayList<>();
        anchors.add(xmlTrustAnchor);

        new Expectations() {{
            simpleCertificateReport.getChain();
            result = chains;

            chainItem.getTrustAnchors();
            result = anchors;

            chainItem.getIndication();
            result = null;

        }};

        Assert.assertFalse(simpleReportTrustAnchorValidatorStep.isValid(simpleCertificateReport));
    }

    @Test
    public void isValidWithAnchorButIndicationFailed(@Mocked final SimpleCertificateReport simpleCertificateReport,
                                                     @Mocked final XmlChainItem chainItem,
                                                     @Mocked final XmlTrustAnchor xmlTrustAnchor) {
        List<XmlChainItem> chains = new ArrayList<>();
        chains.add(chainItem);

        List<XmlTrustAnchor> anchors = new ArrayList<>();
        anchors.add(xmlTrustAnchor);

        new Expectations() {{
            simpleCertificateReport.getChain();
            result = chains;

            chainItem.getTrustAnchors();
            result = anchors;

            chainItem.getIndication();
            result = Indication.FAILED;

        }};

        Assert.assertFalse(simpleReportTrustAnchorValidatorStep.isValid(simpleCertificateReport));
    }

    @Test
    public void isValid(@Mocked final SimpleCertificateReport simpleCertificateReport,
                        @Mocked final XmlChainItem chainItem,
                        @Mocked final XmlTrustAnchor xmlTrustAnchor) {
        List<XmlChainItem> chains = new ArrayList<>();
        chains.add(chainItem);

        List<XmlTrustAnchor> anchors = new ArrayList<>();
        anchors.add(xmlTrustAnchor);

        new Expectations() {{
            simpleCertificateReport.getChain();
            result = chains;

            chainItem.getTrustAnchors();
            result = anchors;

            chainItem.getIndication();
            result = Indication.PASSED;

        }};

        Assert.assertTrue(simpleReportTrustAnchorValidatorStep.isValid(simpleCertificateReport));
    }

}