package eu.domibus.web.rest;

import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.messaging.XmlProcessingException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class PModeResourceTest {

    @Tested
    PModeResource pModeResource;

    @Injectable
    PModeProvider pModeProvider;

    @Test
    public void testDownloadPmodes() {
        // Given
        final byte[] byteA = new byte[]{1, 0, 1};
        new Expectations() {{
            pModeProvider.getRawConfiguration();
            result = byteA;
        }};

        // When
        ResponseEntity<? extends Resource> responseEntity = pModeResource.downloadPmodes();

        // Then
        validateResponseEntity(responseEntity, HttpStatus.OK);
    }

    @Test
    public void tesstDownloadPModesNoContent() {
        // Given
        final byte[] byteA = new byte[]{};
        new Expectations() {{
            pModeProvider.getRawConfiguration();
            result = byteA;
        }};

        // When
        ResponseEntity<? extends Resource> responseEntity = pModeResource.downloadPmodes();

        // Then
        validateResponseEntity(responseEntity, HttpStatus.NO_CONTENT);
    }

    private void validateResponseEntity(ResponseEntity<? extends Resource> responseEntity, HttpStatus httpStatus) {
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(httpStatus, responseEntity.getStatusCode());
        Assert.assertEquals("attachment; filename=Pmodes.xml", responseEntity.getHeaders().get("content-disposition").get(0));
        Assert.assertEquals("Byte array resource [resource loaded from byte array]", responseEntity.getBody().getDescription());
    }

    @Test
    public void testUploadPmodesEmptyFile() {
        // Given
        MultipartFile file = new MockMultipartFile("filename", new byte[]{});
        String description = "description1";

        // When
        ResponseEntity<String> stringResponseEntity = pModeResource.uploadPmodes(file, "description");

        // Then
        Assert.assertNotNull(stringResponseEntity);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, stringResponseEntity.getStatusCode());
        Assert.assertEquals("Failed to upload the PMode file since it was empty.", stringResponseEntity.getBody());
    }

    @Test
    public void testUploadPmodesSuccess() {
        // Given
        MultipartFile file = new MockMultipartFile("filename", new byte[]{1, 0, 1});
        String description = "description1";

        // When
        ResponseEntity<String> stringResponseEntity = pModeResource.uploadPmodes(file, "description");

        // Then
        Assert.assertNotNull(stringResponseEntity);
        Assert.assertEquals(HttpStatus.OK, stringResponseEntity.getStatusCode());
        Assert.assertEquals("PMode file has been successfully uploaded", stringResponseEntity.getBody());
    }

    @Test
    public void testUploadPmodesIssues() throws XmlProcessingException {
        // Given
        MultipartFile file = new MockMultipartFile("filename", new byte[]{1, 0, 1});
        String description = "description1";

        new Expectations() {{
            pModeProvider.updatePModes((byte[]) any, anyString);
            result = new ArrayList<>().add("issue1");
        }};

        // When
        ResponseEntity<String> stringResponseEntity = pModeResource.uploadPmodes(file, "description");

        // Then
        Assert.assertNotNull(stringResponseEntity);
        Assert.assertEquals(HttpStatus.OK, stringResponseEntity.getStatusCode());
        Assert.assertEquals("PMode file has been successfully uploaded but some issues were detected: \ntrue",
                stringResponseEntity.getBody());
    }

    @Test
    public void testUploadPModesXmlProcessingException() throws XmlProcessingException {
        // Given
        MultipartFile file = new MockMultipartFile("filename", new byte[]{1, 0, 1});
        String description = "description1";

        new Expectations() {{
            pModeProvider.updatePModes((byte[]) any, anyString);
            result = new XmlProcessingException("UnitTest1");
        }};

        // When
        ResponseEntity<String> stringResponseEntity = pModeResource.uploadPmodes(file, "description");

        // Then
        Assert.assertNotNull(stringResponseEntity);
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, stringResponseEntity.getStatusCode());
        Assert.assertEquals("Failed to upload the PMode file due to: \n UnitTest1\n",
                stringResponseEntity.getBody());
    }

    @Test
    public void testUploadPModesException() throws XmlProcessingException {
        // Given
        MultipartFile file = new MockMultipartFile("filename", new byte[]{1, 0, 1});
        String description = "description1";

        new Expectations() {{
            pModeProvider.updatePModes((byte[]) any, anyString);
            result = new Exception("UnitTest2");
        }};

        // When
        ResponseEntity<String> stringResponseEntity = pModeResource.uploadPmodes(file, "description");

        // Then
        Assert.assertNotNull(stringResponseEntity);
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, stringResponseEntity.getStatusCode());
        Assert.assertEquals("Failed to upload the PMode file due to: \n UnitTest2",
                stringResponseEntity.getBody());

    }
}
