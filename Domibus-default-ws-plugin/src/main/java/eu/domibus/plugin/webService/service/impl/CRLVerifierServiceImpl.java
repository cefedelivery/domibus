package eu.domibus.plugin.webService.service.impl;

import eu.domibus.plugin.webService.common.exception.AuthenticationException;
import eu.domibus.plugin.webService.service.ICRLVerifierService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.asn1.x509.Extension;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.cert.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by feriaad on 19/06/2015.
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

@Service
public class CRLVerifierServiceImpl implements ICRLVerifierService {

    private static final Logger LOG = LoggerFactory.getLogger(CRLVerifierServiceImpl.class);


    /**
     * Extracts the CRL distribution points from the certificate (if available)
     * and checks the certificate revocation status against the CRLs coming from
     * the distribution points. Supports HTTP, HTTPS, FTP, File based URLs.
     *
     * @param cert the certificate to be checked for revocation
     * @throws AuthenticationException if the certificate is revoked
     */
    @Cacheable(value = "crlByCert", key = "#cert.subjectX500Principal.name")
    public void verifyCertificateCRLs(X509Certificate cert) throws AuthenticationException {
        try {
            List<String> crlDistPoints = getCrlDistributionPoints(cert);
            for (String crlDP : crlDistPoints) {
                X509CRL crl = downloadCRL(crlDP);
                if (crl.isRevoked(cert)) {
                    throw new AuthenticationException("The certificate is revoked by CRL: " + crlDP);
                }
            }
        } catch (Exception ex) {
            if (ex instanceof AuthenticationException) {
                throw (AuthenticationException) ex;
            } else {
                throw new AuthenticationException(
                        "Can not verify CRL for certificate: "
                                + cert.getSubjectX500Principal(), ex);
            }
        }
    }

    /**
     * Downloads CRL from given URL. Supports http, https, ftp based
     * URLs.
     */
    private X509CRL downloadCRL(String crlURL) throws IOException,
            CertificateException, CRLException,
            AuthenticationException, NamingException {
        URL url;
        if (crlURL.startsWith("http://") || crlURL.startsWith("https://")
                || crlURL.startsWith("ftp://") || crlURL.startsWith("file:/")) {
            url = new URL(crlURL);
        } else {
            // try to load from the classpath
            url = Thread.currentThread().getContextClassLoader().getResource("test.crl");
        }
        InputStream crlStream = null;
        try {
            if (url != null) {
                crlStream = url.openStream();
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                return (X509CRL) cf.generateCRL(crlStream);
            } else {
                throw new AuthenticationException(
                        "Can not download CRL from certificate "
                                + "distribution point: " + crlURL);
            }
        } catch (final Exception exc) {
            throw new AuthenticationException(
                    "Can not download CRL from certificate "
                            + "distribution point: " + crlURL, exc);
        } finally {
            if (crlStream != null) {
                crlStream.close();
            }
        }
    }

    /**
     * Extracts all CRL distribution point URLs from the
     * "CRL Distribution Point" extension in a X.509 certificate. If CRL
     * distribution point extension is unavailable, returns an empty list.
     */
    public List<String> getCrlDistributionPoints(X509Certificate cert) throws AuthenticationException {
        byte[] crldpExt = cert.getExtensionValue(Extension.cRLDistributionPoints.getId());
        if (crldpExt == null) {
            return new ArrayList<>();
        }
        ASN1InputStream oAsnInStream = new ASN1InputStream(new ByteArrayInputStream(crldpExt));
        ASN1Primitive derObjCrlDP = null;
        try {
            derObjCrlDP = oAsnInStream.readObject();
        } catch (IOException e) {
            throw new AuthenticationException("Error while extracting CRL distribution point URLs", e);
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
            throw new AuthenticationException("Error while extracting CRL distribution point URLs", e);
        } finally {
            IOUtils.closeQuietly(oAsnInStream2);
        }
        CRLDistPoint distPoint = CRLDistPoint.getInstance(derObj2);
        List<String> crlUrls = new ArrayList<String>();
        for (DistributionPoint dp : distPoint.getDistributionPoints()) {
            DistributionPointName dpn = dp.getDistributionPoint();
            // Look for URIs in fullName
            if (dpn != null
                    && dpn.getType() == DistributionPointName.FULL_NAME) {
                GeneralName[] genNames = GeneralNames.getInstance(
                        dpn.getName()).getNames();
                // Look for an URI
                for (int j = 0; j < genNames.length; j++) {
                    if (genNames[j].getTagNo() == GeneralName.uniformResourceIdentifier) {
                        String url = DERIA5String.getInstance(
                                genNames[j].getName()).getString();
                        crlUrls.add(url);
                    }
                }
            }
        }
        return crlUrls;
    }

    @Override
    @Cacheable(value = "crlByUrl", key = "#crlDistributionPointURL")
    public void verifyCertificateCRLs(String serial, String crlDistributionPointURL) throws AuthenticationException {
        try {
            X509CRL crl = downloadCRL(crlDistributionPointURL);
            if (crl.getRevokedCertificates() != null) {
                for (X509CRLEntry entry : crl.getRevokedCertificates()) {
                    if (new BigInteger(serial.trim().replaceAll("\\s", ""), 16).equals(entry.getSerialNumber())) {
                        throw new AuthenticationException("The certificate is revoked by CRL: " + crlDistributionPointURL);
                    }
                }
            } else {
                LOG.info("The CRL is null for the given certificate");
            }
        } catch (Exception ex) {
            if (ex instanceof AuthenticationException) {
                throw (AuthenticationException) ex;
            } else {
                throw new AuthenticationException("Can not verify CRL for certificate of serial number : " + serial, ex);
            }
        }
    }

}
