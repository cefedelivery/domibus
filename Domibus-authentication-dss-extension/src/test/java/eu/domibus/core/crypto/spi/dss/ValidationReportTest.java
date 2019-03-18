package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.jaxb.detailedreport.DetailedReport;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class ValidationReportTest {


    @Test(expected = IllegalStateException.class)
    public void isValidNoConfiguredConstraints() throws JAXBException {
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream("Validation-report-sample.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(DetailedReport.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final DetailedReport detailedReport = (DetailedReport) unmarshaller.unmarshal(xmlStream);

        ValidationReport validationReport = new ValidationReport();
        Assert.assertFalse(validationReport.isValid(detailedReport, new ArrayList<>()));
    }

    @Test
    public void isValidAnchorAndValidityDate() throws JAXBException {
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream("Validation-report-sample.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(DetailedReport.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final DetailedReport detailedReport = (DetailedReport) unmarshaller.unmarshal(xmlStream);

        final ArrayList<ConstraintInternal> constraints = new ArrayList<>();
        constraints.add(new ConstraintInternal("BBB_XCV_CCCBB", "OK"));
        constraints.add(new ConstraintInternal("BBB_XCV_ICTIVRSC", "OK"));
        ValidationReport validationReport = new ValidationReport();
        Assert.assertTrue(validationReport.isValid(detailedReport, constraints));
    }

    @Test
    public void isValidOneConstraintIsWrong() throws JAXBException {
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream("Validation-report-sample.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(DetailedReport.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final DetailedReport detailedReport = (DetailedReport) unmarshaller.unmarshal(xmlStream);

        final ArrayList<ConstraintInternal> constraints = new ArrayList<>();
        constraints.add(new ConstraintInternal("BBB_XCV_CCCBB", "OK"));
        constraints.add(new ConstraintInternal("BBB_XCV_ICTIVRS", "OK"));
        constraints.add(new ConstraintInternal("QUAL_HAS_CAQC", "OK"));
        ValidationReport validationReport = new ValidationReport();
        Assert.assertFalse(validationReport.isValid(detailedReport, constraints));
    }


}