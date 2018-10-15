package eu.domibus.web.rest;

import eu.domibus.core.logging.LoggingService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.LoggingLevelRO;
import eu.domibus.web.rest.ro.LoggingLevelResponseRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST resource for setting or retrieving logging levels at runtime
 *
 * @author Catalin Enache
 * since 4.1
 */
@RestController
@RequestMapping(value = "/rest/logging")
public class LoggingResource {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(LoggingResource.class);

    @Autowired
    LoggingService loggingService;

    @PostMapping(value = "/loglevel")
    public ResponseEntity<List<LoggingLevelResponseRO>> setLogLevel(@RequestBody List<LoggingLevelRO> loggingLevelROS) {

        final List<LoggingLevelResponseRO> loggingLevelResponseROS = loggingLevelROS.
                stream().
                map(loggingLevelRO -> loggingService.setLoggingLevel(loggingLevelRO)).
                collect(Collectors.toList());

        return ResponseEntity.ok().body(loggingLevelResponseROS);

    }

    @GetMapping(value = "/loglevel")
    public ResponseEntity<List<LoggingLevelRO>> getLogLevel(@RequestParam(value = "name", defaultValue = "eu.domibus", required = false) String name,
                                                            @RequestParam(value = "showClasses", defaultValue = "false", required = false) boolean showClasses) {

        final List<LoggingLevelRO> loggingLevelResultROS = loggingService.getLoggingLevel(name, showClasses);

        return ResponseEntity.ok().body(loggingLevelResultROS);

    }

}
