package eu.domibus.ext.rest;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice {

	private DomibusLogger LOG = DomibusLoggerFactory.getLogger(ExceptionControllerAdvice.class);
 
	@ExceptionHandler(Exception.class)
    public ResponseEntity exceptionHandler(Exception ex) {
		LOG.error("Generic error occurred", ex);
		return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}