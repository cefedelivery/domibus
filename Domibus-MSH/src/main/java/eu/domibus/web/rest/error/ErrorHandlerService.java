package eu.domibus.web.rest.error;

import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author Ion Perpegel
 * @since 4.1
 * <p>
 * A service for packaging errors as REST Responses
 * It is called from the global error handler as well as from custom error handlers
 * It closes the connection in order to avoid a chrome-tomcat combination error
 */

@Service
public class ErrorHandlerService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(ErrorHandlerService.class);

    public ResponseEntity<ErrorRO> createResponse(Throwable ex) {
        return this.createResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<ErrorRO> createResponse(Throwable ex, HttpStatus status) {
        LOG.error(ex.getMessage(), ex);

        HttpHeaders headers = new HttpHeaders();
        //We need to send the connection header for the tomcat/chrome combination to be able to read the error message
        headers.set(HttpHeaders.CONNECTION, "close");

        return new ResponseEntity(new ErrorRO(ex.getMessage()), headers, status);
    }
}
