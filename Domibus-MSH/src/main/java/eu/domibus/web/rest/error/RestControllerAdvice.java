package eu.domibus.web.rest.error;

import eu.domibus.api.multitenancy.DomainException;
import eu.domibus.ext.rest.ErrorRO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.RollbackException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.0
 * <p>
 * A global error handler for REST interfaces;
 * the last resort if the error is not caught in the controller where it originated
 */

@ControllerAdvice
@RequestMapping(produces = "application/vnd.error+json")
public class RestControllerAdvice extends ResponseEntityExceptionHandler {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RestControllerAdvice.class);

    @Autowired
    private ErrorHandlerService errorHandlerService;

    @ExceptionHandler({DomainException.class})
    public ResponseEntity<ErrorRO> handleDomainException(DomainException ex) {
        return handleWrappedException(ex);
    }

    @ExceptionHandler({RollbackException.class})
    public ResponseEntity<ErrorRO> handleRollbackException(RollbackException ex) {
        return handleWrappedException(ex);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorRO> handleException(Exception ex) {
        return errorHandlerService.createResponse(ex);
    }

    private ResponseEntity<ErrorRO> handleWrappedException(Exception ex) {
        Throwable rootCause = ExceptionUtils.getRootCause(ex) == null ? ex : ExceptionUtils.getRootCause(ex);

        return errorHandlerService.createResponse(rootCause);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> list = ex.getBindingResult()
                .getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());
        return new ResponseEntity(list, HttpStatus.CONFLICT);
    }
}
