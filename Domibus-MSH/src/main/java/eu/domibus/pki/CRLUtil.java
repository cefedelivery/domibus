package eu.domibus.pki;

import eu.domibus.api.util.HttpUtil;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cosmin Baciu on 11-Jul-16.
 */
@Service
public class CRLUtil {

    @Autowired
    HttpUtil httpUtil;

    /**
     * Downloads CRL from the given URL. Supports loading the crl using http, https, ftp based,  classpath
     */
    public X509CRL downloadCRL(String crlURL) throws DomibusCRLException {
        URL url;
        try {
            url = getCrlURL(crlURL);
        } catch (MalformedURLException e) {
            throw new DomibusCRLException(e);
        }

        if (url == null) {
            throw new DomibusCRLException("Could not get the CRL for distribution point [" + crlURL + "]");
        }

        InputStream crlStream = null;
        try {
            crlStream = getCrlInputStream(url);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509CRL) cf.generateCRL(crlStream);
        } catch (final Exception exc) {
            throw new DomibusCRLException("Can not download CRL from pki distribution point: " + crlURL, exc);
        } finally {
            IOUtils.closeQuietly(crlStream);
        }
    }

    public BigInteger parseCertificateSerial(String serial) {
        return new BigInteger(serial.trim().replaceAll("\\s", ""), 16);
    }

    protected InputStream getCrlInputStream(URL crlURL) throws IOException {
        InputStream result = null;
        if (crlURL.toString().startsWith("http://") || crlURL.toString().startsWith("https://")) {
            result = httpUtil.downloadURL(crlURL.toString());
        } else {
            result = crlURL.openStream();
        }
        return result;
    }

    public URL getCrlURL(String crlURL) throws MalformedURLException {
        return isURLSupported(crlURL) ? new URL(crlURL) : getResourceFromClasspath(crlURL);
    }

    public boolean isURLSupported(String crlURL) {
        if (crlURL.startsWith("http://") ||
                crlURL.startsWith("https://") ||
                crlURL.startsWith("ftp://") ||
                crlURL.startsWith("file:/"))
            return true;
        return false;
    }

    public URL getResourceFromClasspath(String url) {
        return Thread.currentThread().getContextClassLoader().getResource(url);
    }

    /**
     * Extracts all CRL distribution point URLs from the "CRL Distribution Point" extension of X.509 pki.
     * If the CRL distribution point extension is unavailable, returns an empty list.
     */
    public List<String> getCrlDistributionPoints(X509Certificate cert) {
        byte[] crldpExt = cert.getExtensionValue(org.bouncycastle.asn1.x509.Extension.cRLDistributionPoints.getId());
        if (crldpExt == null) {
            return new ArrayList<>();
        }
        ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crldpExt));
        ASN1Primitive derObjCrlDP = null;
        try {
            derObjCrlDP = oAsnInStream.readObject();
        } catch (IOException e) {
            throw new DomibusCRLException("Error while extracting CRL distribution point URLs", e);
        } finally {
            IOUtils.closeQuietly(oAsnInStream);
        }
        DEROctetString dosCrlDP = (DEROctetString) derObjCrlDP;
        byte[] crldpExtOctets = dosCrlDP.getOctets();
        ASN1InputStream oAsnInStream2 = new ASN1InputStream(new ByteArrayInputStream(crldpExtOctets));
        ASN1Primitive derObj2 = null;
        try {
            derObj2 = oAsnInStream2.readObject();
        } catch (IOException e) {
            throw new DomibusCRLException("Error while extracting CRL distribution point URLs", e);
        } finally {
            IOUtils.closeQuietly(oAsnInStream2);
        }
        CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
        List<String> crlUrls = new ArrayList<String>();
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            DistributionPointName dpn = dp.getDistributionPoint();
            // Look for URIs in fullName
            if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME) {
                GeneralName[] genNames = GeneralNames.getInstance(dpn.getName()).getNames();
                // Look for an URI
                for (int index = 0; index < genNames.length; index++) {
                    if (genNames[index].getTagNo() == GeneralName.uniformResourceIdentifier) {
                        String url = DERIA5String.getInstance(genNames[index].getName()).getString();
                        crlUrls.add(url);
                    }
                }
            }
        }
        return crlUrls;
    }
}
