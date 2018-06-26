package eu.domibus.core.alerts;

import eu.domibus.core.alerts.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/rest/alert")
public class AlertResource {

    private final static Logger LOG = LoggerFactory.getLogger(AlertResource.class);

    @Autowired
    private AlertService alertService;
}
