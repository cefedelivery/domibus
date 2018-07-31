package eu.domibus.api.security;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class BlueCoatClientCertificateAuthenticationTest {

    @Test
    public void testRead() throws UnsupportedEncodingException, ParseException {
        String serial = "123ABCD";
        String issuer = "CN=PEPPOL SERVICE METADATA PUBLISHER TEST CA,OU=FOR TEST PURPOSES ONLY,O=NATIONAL IT AND TELECOM AGENCY,C=DK";
        String subject = "O=DG-DIGIT,CN=SMP_1000000007,C=BE";
        DateFormat df = new SimpleDateFormat("MMM d hh:mm:ss yyyy zzz", Locale.US);
        Date validFrom = df.parse("Jun 01 10:37:53 2015 CEST");
        Date validTo = df.parse("Jun 01 10:37:53 2035 CEST");

        String certHeaderValue = "serial=" + serial + "&subject=" + subject + "&validFrom="+ df.format(validFrom) +"&validTo=" + df.format(validTo) +"&issuer=" + issuer;
        BlueCoatClientCertificateAuthentication bcAuth = new BlueCoatClientCertificateAuthentication(certHeaderValue);

        Assert.assertEquals(serial, ((CertificateDetails) bcAuth.getCredentials()).getSerial());
        Assert.assertEquals(issuer, ((CertificateDetails) bcAuth.getCredentials()).getIssuer());
        Assert.assertEquals(validFrom, ((CertificateDetails) bcAuth.getCredentials()).getValidFrom().getTime());
        Assert.assertEquals(validTo, ((CertificateDetails) bcAuth.getCredentials()).getValidTo().getTime());
    }

    /**
     * The order of the certificate attributes is different and there are spaces after the commas. The certificate must be valid anyway
     * @throws UnsupportedEncodingException
     * @throws ParseException
     */
    @Test
    public void testReadDifferentOrderWithSpaces() throws UnsupportedEncodingException, ParseException {
        String serial = "123ABCD";
        String issuer = "C=DK, CN=PEPPOL SERVICE METADATA PUBLISHER TEST CA, O=NATIONAL IT AND TELECOM AGENCY, OU=FOR TEST PURPOSES ONLY";
        String subject = "C=BE, O=DG-DIGIT, CN=SMP_1000000007";
        DateFormat df = new SimpleDateFormat("MMM d hh:mm:ss yyyy zzz", Locale.US);
        Date validFrom = df.parse("Jun 01 10:37:53 2015 CEST");
        Date validTo = df.parse("Jun 01 10:37:53 2035 CEST");

        String certHeaderValue = "serial=" + serial + "&subject=" + subject + "&validFrom="+ df.format(validFrom) +"&validTo=" + df.format(validTo) +"&issuer=" + issuer;
        BlueCoatClientCertificateAuthentication bcAuth = new BlueCoatClientCertificateAuthentication(certHeaderValue);

        Assert.assertEquals(serial, ((CertificateDetails) bcAuth.getCredentials()).getSerial());
        Assert.assertEquals(issuer, ((CertificateDetails) bcAuth.getCredentials()).getIssuer());
        Assert.assertEquals(validFrom, ((CertificateDetails) bcAuth.getCredentials()).getValidFrom().getTime());
        Assert.assertEquals(validTo, ((CertificateDetails) bcAuth.getCredentials()).getValidTo().getTime());
    }

    /**
     * The order of the header (serial, subject, etc.) is different
     * @throws UnsupportedEncodingException
     * @throws ParseException
     */
    @Test
    public void testReadDifferentOrderWithSpaces2() throws UnsupportedEncodingException, ParseException {
        String serial = "123ABCD";
        // different order for the issuer certificate with extra spaces
        String issuer = "CN=PEPPOL SERVICE METADATA PUBLISHER TEST CA,  C=DK, O=NATIONAL IT AND TELECOM AGENCY,    OU=FOR TEST PURPOSES ONLY";
        String subject = "C=BE, O=DG-DIGIT, CN=SMP_1000000007";
        DateFormat df = new SimpleDateFormat("MMM d hh:mm:ss yyyy zzz", Locale.US);
        Date validFrom = df.parse("Jun 01 10:37:53 2015 CEST");
        Date validTo = df.parse("Jun 01 10:37:53 2035 CEST");

        // case insensitivity test
        String certHeaderValue = "iSsUeR=" + issuer  + "&VaLiDFrOm="+ df.format(validFrom) + "&sUbJecT=" + subject + "&VALidTo=" + df.format(validTo)  + "&serIAL=" + serial;
        BlueCoatClientCertificateAuthentication bcAuth = new BlueCoatClientCertificateAuthentication(certHeaderValue);

        Assert.assertEquals(serial, ((CertificateDetails) bcAuth.getCredentials()).getSerial());
        Assert.assertEquals(issuer, ((CertificateDetails) bcAuth.getCredentials()).getIssuer());
        Assert.assertEquals(validFrom, ((CertificateDetails) bcAuth.getCredentials()).getValidFrom().getTime());
        Assert.assertEquals(validTo, ((CertificateDetails) bcAuth.getCredentials()).getValidTo().getTime());
    }

    /**
     * Test the construction of the Authenticator from real values from the BlueCoat proxy
     * @throws UnsupportedEncodingException
     * @throws ParseException
     */
    @Test
    public void calculateCertificateId() {
        String certHeader = "sno=53%3Aef%3A79%3Ac3%3A54%3A98%3Abb%3A63%3A38%3A35%3A9a%3A19%3A5d%3A2d%3Ad8%3A8c&subject=C%3DBE%2C+O%3DDG-DIGIT%2C+CN%3DSMP_1000000007&validfrom=Oct+21+00%3A00%3A00+2014+GMT&validto=Oct+20+23%3A59%3A59+2016+GMT&issuer=C%3DDK%2C+O%3DNATIONAL+IT+AND+TELECOM+AGENCY%2C+OU%3DFOR+TEST+PURPOSES+ONLY%2C+CN%3DPEPPOL+SERVICE+METADATA+PUBLISHER+TEST+CA";
        BlueCoatClientCertificateAuthentication auth = new BlueCoatClientCertificateAuthentication(certHeader);
        Assert.assertEquals("CN=SMP_1000000007,O=DG-DIGIT,C=BE:53ef79c35498bb6338359a195d2dd88c", auth.getName());
    }
}
