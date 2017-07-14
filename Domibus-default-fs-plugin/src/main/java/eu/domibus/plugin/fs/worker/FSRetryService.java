/*
 * Copyright 2017 Domibus FS Plugin Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/sites/default/files/eupl1.1.-licence-en_0.pdf
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */
package eu.domibus.plugin.fs.worker;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@Service
public class FSRetryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSRetryService.class);

    @Autowired
    @Qualifier("fsPluginProperties")
    private Properties fsPluginProperties;

    /**
     * Triggering the re-send means that the message file from the FAILED directory will be copied directly under the
     * corresponding OUT directory and eventually it will be treated like a normal file.
     */
    public void resendFailedFSMessages() {
        LOG.info("Resending failed file system messages...");
    }

}
