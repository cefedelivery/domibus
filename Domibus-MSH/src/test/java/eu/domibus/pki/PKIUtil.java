package eu.domibus.pki;

import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.x509.X509V2CRLGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.junit.Test;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 * Created by Cosmin Baciu on 08-Jul-16.
 */
public class PKIUtil {

    public X509CRL createCRL(List<BigInteger> revokedSerialNumbers) throws NoSuchAlgorithmException, SignatureException, NoSuchProviderException, InvalidKeyException {
        KeyPair caKeyPair = generateKeyPair();

        X509V2CRLGenerator crlGen = new X509V2CRLGenerator();
        Date now = new Date();
        crlGen.setIssuerDN(new X500Principal("CN=GlobalSign Root CA"));
        crlGen.setThisUpdate(now);
        crlGen.setNextUpdate(new Date(now.getTime() + 60 * 1000));
        crlGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

        if (revokedSerialNumbers != null) {
            for (BigInteger revokedSerialNumber : revokedSerialNumbers) {
                crlGen.addCRLEntry(revokedSerialNumber, now, CRLReason.privilegeWithdrawn);
            }
        }

        crlGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caKeyPair.getPublic()));
        crlGen.addExtension(X509Extensions.CRLNumber, false, new CRLNumber(BigInteger.valueOf(1)));

        return crlGen.generateX509CRL(caKeyPair.getPrivate(), "BC");
    }

    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    public X509Certificate createCertificate(BigInteger serial, Date startDate, Date expiryDate, List<String> crlUrls) throws SignatureException, NoSuchProviderException, InvalidKeyException, NoSuchAlgorithmException, CertificateEncodingException {
        KeyPair key = generateKeyPair();

        X509V3CertificateGenerator generator = new X509V3CertificateGenerator();
        generator.setSubjectDN(new X509Name("C=BE, O=GlobalSign nv-sa, OU=Root CA"));
        X500Principal subjectName = new X500Principal("CN=GlobalSign Root CA");
        generator.setIssuerDN(subjectName);
        generator.setSerialNumber(serial);
        generator.setNotBefore(startDate);
        generator.setNotAfter(expiryDate);
        generator.setPublicKey(key.getPublic());
        generator.setSignatureAlgorithm("SHA256WithRSAEncryption");

        if (crlUrls != null) {
            DistributionPoint[] distPoints = createDistributionPoints(crlUrls);
            generator.addExtension(Extension.cRLDistributionPoints, false, new CRLDistPoint(distPoints));
        }

        X509Certificate x509Certificate = generator.generate(key.getPrivate(), "BC");
        return x509Certificate;
    }

    public X509Certificate createCertificate(BigInteger serial, List<String> crlUrls) throws SignatureException, NoSuchProviderException, InvalidKeyException, NoSuchAlgorithmException, CertificateEncodingException {
        return createCertificate(serial, new Date(), new Date(), crlUrls);
    }

    public DistributionPoint[] createDistributionPoints(List<String> crlUrls) {
        List<DistributionPoint> result = new ArrayList<>();
        for (String crlUrl : crlUrls) {
            DistributionPointName distPointOne = new DistributionPointName(
                    new GeneralNames(
                            new GeneralName(GeneralName.uniformResourceIdentifier, crlUrl)
                    )
            );
            result.add(new DistributionPoint(distPointOne, null, null));
        }


        return result.toArray(new DistributionPoint[0]);
    }

    @Test
    public void name() throws CertificateException {
        String initialString = "MIIDNzCCAh+gAwIBAgIJAJeSzmm8ifFMMA0GCSqGSIb3DQEBCwUAMDIxDzANBgNVBAMMBnJlZF9n&#xd;\n" +
                "                dzESMBAGA1UECgwJZURlbGl2ZXJ5MQswCQYDVQQGEwJCRTAeFw0xNzA5MTQwNzI2NDdaFw0yNTEy&#xd;\n" +
                "                MDEwNzI2NDdaMDIxDzANBgNVBAMMBnJlZF9ndzESMBAGA1UECgwJZURlbGl2ZXJ5MQswCQYDVQQG&#xd;\n" +
                "                EwJCRTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAMqaG7svX3VYSCnI3DL3aOLWCMJj&#xd;\n" +
                "                jSY9103Z33lUMOc1PCusakhLDQec0WB9MLAxxtf1JNgKBjzjDRod0jsq67ubxKNyn2L6VWEoeUBX&#xd;\n" +
                "                LbgNypuzZ2czbKyjJ0cMa7TtOyZro06SNcKEVIxWgW8o44tIHKPAtSOxDRriIGPatN+phmBejSCC&#xd;\n" +
                "                bjZeN5IB5m1n4cQaV5zK7xf/MdRpdikEPqwdD2gCh84Npi6uEnZjXQvIfx5yMBimIbSUWSJ4/bgk&#xd;\n" +
                "                DcBW1gbljFJcL47F0+agH2aM1xEUh/1bG+XwUMgyt3ekh8OR+9VO58qmA7SrbzrB8MIz6iVmeolZ&#xd;\n" +
                "                625ZtwjmDwECAwEAAaNQME4wHQYDVR0OBBYEFOwCXgJxC+RBOZd5S3PhKBaTnyQqMB8GA1UdIwQY&#xd;\n" +
                "                MBaAFOwCXgJxC+RBOZd5S3PhKBaTnyQqMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQELBQADggEB&#xd;\n" +
                "                ADHgWnjAhWRHpdgla9ajq+ugHNNdZQKWOD7RWjAP6A8TAbf9lJ8kyn3anqrbzotzJT1/L+DF3Wcp&#xd;\n" +
                "                JS6q779C8XyQUdtdEDNpj+D5JeeAivBat1TmnwY7Ud+Uo3tSeByyDMkFEyYXjRDDa+MpbE/Ya5Mw&#xd;\n" +
                "                jb2JbWzX1FnoVTkM5rjDrrT+CDFpDij6xQuNbo+BFUXk3M3MTJh0IESn12IDMblnvw3pb8ec+5ol&#xd;\n" +
                "                hbiZkVe/V43FEMRoIRQHot62qkF1eHsCj75eby5E+sufEQXyY7vSCI8lDCkXn7nYEYKB+oNbg48/&#xd;\n" +
                "                Rc4oVzEIwS5Le/oxOzT3xOBCbVwtcXI4HW7jUQo=";
        initialString = initialString.replaceAll("&#xd;\\\n", "");
        initialString = initialString.replaceAll("                ", "");
        System.out.println(initialString);
        final byte[] decode = Base64.getDecoder().decode(initialString);


        InputStream targetStream = new ByteArrayInputStream(decode);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) factory.generateCertificate(targetStream);
        System.out.println(cert);
    }
}
