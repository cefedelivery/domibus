package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.jaxb.simplecertificatereport.SimpleCertificateReport;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
public interface SimpleReportValidationStep {

    boolean isValid(SimpleCertificateReport simpleCertificateReport);

}
