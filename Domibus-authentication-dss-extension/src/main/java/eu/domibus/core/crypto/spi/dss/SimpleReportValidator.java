package eu.domibus.core.crypto.spi.dss;


import eu.europa.esig.dss.jaxb.simplecertificatereport.SimpleCertificateReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.0
 * <p>
 * Dss validation produces Simple report element. This class validate simple reports.
 */
@Component
public class SimpleReportValidator {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleReportValidator.class);

    private List<SimpleReportValidationStep> simpleReportValidationSteps;

    @Autowired
    public SimpleReportValidator(List<SimpleReportValidationStep> simpleReportValidationSteps) {
        this.simpleReportValidationSteps = simpleReportValidationSteps;
    }

    public boolean isValid(SimpleCertificateReport simpleCertificateReport) {
        boolean valid = true;
        for (SimpleReportValidationStep simpleReportValidationStep : simpleReportValidationSteps) {
            valid = valid && simpleReportValidationStep.isValid(simpleCertificateReport);
        }
        return valid;
    }
}
