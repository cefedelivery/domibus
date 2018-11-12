package eu.domibus.web.rest.error;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.PolicyServiceImpl;
import eu.domibus.web.rest.error.ErrorHandlerService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.Bus;
import org.apache.neethi.Policy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@RunWith(JMockit.class)
public class ErrorHandlerServiceTest {

    @Tested
    ErrorHandlerService errorHandlerService;

    @Test
    public void testCreateResponseWithStatus() {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = "Error occurred";
        Exception ex = new Exception(errorMessage);

        ResponseEntity<ErrorRO> result = errorHandlerService.createResponse(ex, status);

        assertEquals(status, result.getStatusCode());
        assertEquals("close", result.getHeaders().get(HttpHeaders.CONNECTION).get(0));
        assertEquals(errorMessage, result.getBody().getMessage());
    }

    @Test
    public void testCreateResponse() {
        String errorMessage = "Error occurred";
        Exception ex = new Exception(errorMessage);

        ResponseEntity<ErrorRO> result = errorHandlerService.createResponse(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("close", result.getHeaders().get(HttpHeaders.CONNECTION).get(0));
        assertEquals(errorMessage, result.getBody().getMessage());
    }
}
