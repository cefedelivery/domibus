package eu.domibus.pki;

import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.x509.X509V2CRLGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;

import javax.security.auth.x500.X500Principal;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
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

        if(revokedSerialNumbers != null) {
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

        if(crlUrls != null) {
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

}
