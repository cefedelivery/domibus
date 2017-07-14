package eu.domibus.web.rest;

import eu.domibus.common.services.DomibusCacheService;
import eu.domibus.wss4j.common.crypto.CryptoService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

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
}
