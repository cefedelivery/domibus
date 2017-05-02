/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.ebms3.sender;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.jsse.TLSClientParametersConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class TLSReader {
    public static final String CLIENTAUTHENTICATION_XML = "clientauthentication.xml";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSReader.class);

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Cacheable("tlsCache")
    public TLSClientParameters getTlsClientParameters() {
        byte[] encoded = new byte[0];
        String config = "";
        try {
            encoded = Files.readAllBytes(Paths.get(domibusConfigurationService.getConfigLocation(), CLIENTAUTHENTICATION_XML));
            config = new String(encoded, "UTF-8");
            //TODO this replacement should be extracted into a service method
            config = config.replaceAll("\\Q${domibus.config.location}\\E", domibusConfigurationService.getConfigLocation().replace('\\', '/'));

            return (TLSClientParameters) TLSClientParametersConfig.createTLSClientParameters(config);

        } catch (final FileNotFoundException fnfEx) {
            LOG.warn("No tls configuration file " + domibusConfigurationService.getConfigLocation() + CLIENTAUTHENTICATION_XML + " found. Mutual authentication will not be supported.");
            LOG.trace("", fnfEx);
            return null;
        } catch (IOException | RuntimeException ex) {
            LOG.warn("Mutual authentication will not be supported.");
            LOG.trace("", ex);
            return null;
        }
    }
}
