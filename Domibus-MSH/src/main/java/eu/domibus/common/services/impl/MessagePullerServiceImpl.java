package eu.domibus.common.services.impl;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by dussath on 5/23/17.
 * <p>
 * This class will check recurently (based on a cron configuration) in the  PMODE config to find potential pull configurations.
 * If pull configurations are found, it will create a pullrequest.
 */
@Service
public class MessagePullerServiceImpl implements MessagePullerService {
    private final static DomibusLogger LOG = DomibusLoggerFactory.getLogger(MessagePullerServiceImpl.class);


}
