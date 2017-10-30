package eu.domibus.web.rest;

import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.web.rest.ro.PModeResponseRO;
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
import java.util.Date;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class PModeResourceTest {

    @Tested
    private PModeResource pModeResource;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Test
    public void testDownloadPmodes() {
        // Given
        final byte[] byteA = new byte[]{1, 0, 1};
        new Expectations() {{
            pModeProvider.getPModeFile(0);
            result = byteA;
        }};

        // When
        ResponseEntity<? extends Resource> responseEntity = pModeResource.downloadPmode(0);

        // Then
        validateResponseEntity(responseEntity, HttpStatus.OK);
    }

    @Test
    public void testDownloadPModesNoContent() {
        // Given
        final byte[] byteA = new byte[]{};
        new Expectations() {{
            pModeProvider.getPModeFile(0);
            result = byteA;
        }};

        // When
        ResponseEntity<? extends Resource> responseEntity = pModeResource.downloadPmode(0);

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

    @Test
    public void testDeletePmodesEmptyList() {
        // Given
        final ArrayList<String> emptyList = new ArrayList<>();

        // When
        final ResponseEntity<String> stringResponseEntity = pModeResource.deletePmodes(emptyList);

        // Then
        Assert.assertNotNull(stringResponseEntity);
        Assert.assertEquals(HttpStatus.BAD_REQUEST, stringResponseEntity.getStatusCode());
        Assert.assertEquals("Failed to delete PModes since the list of ids was empty.", stringResponseEntity.getBody());
    }

    @Test
    public void testDeletePmodesSuccess() {
        // Given
        List<String> stringList = new ArrayList<>();
        stringList.add("1");
        stringList.add("2");

        // When
        final ResponseEntity<String> stringResponseEntity = pModeResource.deletePmodes(stringList);

        // Then
        Assert.assertNotNull(stringResponseEntity);
        Assert.assertEquals(HttpStatus.OK, stringResponseEntity.getStatusCode());
        Assert.assertEquals("PModes were deleted\n", stringResponseEntity.getBody());
    }

    @Test
    public void testDeletePmodesException() {
        // Given
        final Exception exception = new Exception("Mocked exception");
        List<String> stringList = new ArrayList<>();
        stringList.add("1");

        new Expectations(pModeResource) {{
            pModeProvider.removePMode(anyInt);
            result = exception;
        }};

        // When
        final ResponseEntity<String> stringResponseEntity = pModeResource.deletePmodes(stringList);

        // Then
        Assert.assertNotNull(stringResponseEntity);
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, stringResponseEntity.getStatusCode());
        Assert.assertEquals("Impossible to delete PModes due to \nMocked exception", stringResponseEntity.getBody());
    }


    @Test
    public void testUploadPmodeSuccess() {
        // Given
        // When
        final ResponseEntity<String> stringResponseEntity = pModeResource.uploadPmode(1);

        // Then
        Assert.assertNotNull(stringResponseEntity);
        Assert.assertEquals(HttpStatus.OK, stringResponseEntity.getStatusCode());
        Assert.assertEquals("PMode was successfully uploaded", stringResponseEntity.getBody());
    }

    @Test
    public void testUploadPmodeException() throws XmlProcessingException {
        // Given
        final Exception exception = new Exception("Mocked exception");
        new Expectations(pModeResource) {{
            pModeProvider.updatePModes((byte[]) any, anyString);
            result = exception;
        }};

        // When
        final ResponseEntity<String> stringResponseEntity = pModeResource.uploadPmode(1);

        // Then
        Assert.assertNotNull(stringResponseEntity);
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, stringResponseEntity.getStatusCode());
        Assert.assertEquals("Impossible to upload PModes due to \nMocked exception", stringResponseEntity.getBody());
    }

    @Test
    public void testUploadPmodeIssues() throws XmlProcessingException {
        // Given
        List<String> issues = new ArrayList<>();
        issues.add("issue1");
        new Expectations(pModeResource) {{
           pModeProvider.updatePModes((byte[]) any, anyString);
           result = issues;
        }};

        // When
        final ResponseEntity<String> stringResponseEntity = pModeResource.uploadPmode(1);

        // Then
        Assert.assertNotNull(stringResponseEntity);
        Assert.assertEquals(HttpStatus.OK, stringResponseEntity.getStatusCode());
        Assert.assertEquals("PMode was successfully uploaded but some issues were detected: \nissue1", stringResponseEntity.getBody());
    }

    @Test
    public void testPmodeList() {
        // Given
        final Date date = new Date();
        final String username = "username";
        final String description = "description";

        PModeResponseRO pModeResponseRO = new PModeResponseRO();
        pModeResponseRO.setId(1);
        pModeResponseRO.setUsername(username);
        pModeResponseRO.setDescription(description);
        pModeResponseRO.setConfigurationDate(date);
        pModeResponseRO.setCurrent(true);

        ArrayList<PModeResponseRO> pModeResponseROArrayList = new ArrayList<>();
        pModeResponseROArrayList.add(pModeResponseRO);

        new Expectations(pModeResource) {{
           domainConverter.convert((List<PModeArchiveInfo>)any, PModeResponseRO.class);
           result = pModeResponseROArrayList;
        }};

        // When
        final List<PModeResponseRO> pModeResponseROSGot = pModeResource.pmodeList();

        // Then
        Assert.assertEquals(1, pModeResponseROSGot.size());
        Assert.assertEquals(1, pModeResponseRO.getId());
        Assert.assertEquals(date, pModeResponseRO.getConfigurationDate());
        Assert.assertEquals(username, pModeResponseRO.getUsername());
        Assert.assertEquals(description, pModeResponseRO.getDescription());
        Assert.assertTrue(pModeResponseRO.isCurrent());

    }
}
