package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.jaxb.simplecertificatereport.SimpleCertificateReport;
import eu.europa.esig.dss.jaxb.simplecertificatereport.XmlChainItem;
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
 * @since 4.0
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
}