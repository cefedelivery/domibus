package eu.domibus.web.rest;

import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.common.services.impl.CsvServiceImpl;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.pki.CertificateService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import eu.domibus.wss4j.common.crypto.CryptoService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class TruststoreResourceTest {

    @Tested
    TruststoreResource truststoreResource;

    @Injectable
    CryptoService cryptoService;

    @Injectable
    DomibusCacheService domibusCacheService;

    @Injectable
    CertificateService certificateService;

    @Injectable
    DomainCoreConverter domainConverter;

    @Injectable
    private CsvServiceImpl csvServiceImpl;

    @Test
    public void testUploadTruststoreFileSuccess() {
        // Given
        MultipartFile multiPartFile = new MockMultipartFile("filename", new byte[] {1,0,1});

        // When
        ResponseEntity<String> responseEntity = truststoreResource.uploadTruststoreFile(multiPartFile, "pass");

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals("Truststore file has been successfully replaced.", responseEntity.getBody());
    }

    @Test
    public void testUploadTruststoreEmpty() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile("emptyfile", new byte[] {});

        // When
        ResponseEntity<String> responseEntity = truststoreResource.uploadTruststoreFile(emptyFile, "pass");

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assert.assertEquals("Failed to upload the truststore file since it was empty.", responseEntity.getBody());
    }

    @Test
    public void testUploadTruststoreException() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        // Given
        MultipartFile multiPartFile = new MockMultipartFile("filename", new byte[] {1,0,1});

        new Expectations() {{
            cryptoService.replaceTruststore((byte[]) any, anyString);
            result = new KeyStoreException("Impossible to access keystore");
        }};

        // When
        ResponseEntity<String> responseEntity = truststoreResource.uploadTruststoreFile(multiPartFile, "pass");

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        Assert.assertEquals("Failed to upload the truststore file due to => Impossible to access keystore", responseEntity.getBody());
    }

    private List<TrustStoreRO> getTestTrustStoreROList(Date date) {
        List<TrustStoreRO> trustStoreROList = new ArrayList<>();
        TrustStoreRO trustStoreRO = new TrustStoreRO();
        trustStoreRO.setName("Name");
        trustStoreRO.setSubject("Subject");
        trustStoreRO.setIssuer("Issuer");
        trustStoreRO.setValidFrom(date);
        trustStoreRO.setValidUntil(date);
        trustStoreROList.add(trustStoreRO);
        return trustStoreROList;
    }

    @Test
    public void testTrustStoreEntries() {
        // Given
        Date date = new Date();
        List<TrustStoreEntry> trustStoreEntryList = new ArrayList<>();
        TrustStoreEntry trustStoreEntry = new TrustStoreEntry("Name", "Subject", "Issuer", date, date);
        trustStoreEntryList.add(trustStoreEntry);

        new Expectations() {{
            certificateService.getTrustStoreEntries();
            result = trustStoreEntryList;
            domainConverter.convert(trustStoreEntryList, TrustStoreRO.class);
            result = getTestTrustStoreROList(date);
        }};

        // When
        final List<TrustStoreRO> trustStoreROList = truststoreResource.trustStoreEntries();

        // Then
        Assert.assertEquals(getTestTrustStoreROList(date), trustStoreROList);
    }

    @Test
    public void testGetCsv() throws EbMS3Exception {
        // Given
        Date date = new Date();
        List<TrustStoreRO> trustStoreROList = getTestTrustStoreROList(date);
        new Expectations(truststoreResource) {{
            truststoreResource.trustStoreEntries();
            result = trustStoreROList;
            csvServiceImpl.exportToCSV(trustStoreROList);
            result = "Name, Subject, Issuer, Valid From, Valid Until" + System.lineSeparator() +
                    "Name, Subject, Issuer, " + date + ", " + date + System.lineSeparator();
        }};

        // When
        final ResponseEntity<String> csv = truststoreResource.getCsv();

        // Then
        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
        Assert.assertEquals("Name, Subject, Issuer, Valid From, Valid Until" + System.lineSeparator() +
                        "Name, Subject, Issuer, " + date + ", " + date + System.lineSeparator(),
                csv.getBody());
    }
}
