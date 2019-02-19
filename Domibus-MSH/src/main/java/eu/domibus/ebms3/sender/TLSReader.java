
package eu.domibus.ebms3.sender;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.jsse.TLSClientParametersConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class TLSReader {
    public static final String CLIENT_AUTHENTICATION_XML = "clientauthentication.xml";

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TLSReader.class);

    public static final String REGEX_DOMIBUS_CONFIG_LOCATION = "\\Q${domibus.config.location}\\E";

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Cacheable("tlsCache")
    public TLSClientParameters getTlsClientParameters(String domainCode) {
        Optional<Path> path = getClientAuthenticationPath(domainCode);
        if(path.isPresent()) {
            try {
                byte[] encoded = Files.readAllBytes(path.get());
                String config = new String(encoded, "UTF-8");
                //TODO this replacement should be extracted into a service method
                config = config.replaceAll(REGEX_DOMIBUS_CONFIG_LOCATION, domibusConfigurationService.getConfigLocation().replace('\\', '/'));

                return (TLSClientParameters) TLSClientParametersConfig.createTLSClientParameters(config);
            } catch (Exception e) {
                LOG.warn("Mutual authentication will not be supported for [{}]", path);
                LOG.trace("", e);
            }
        }
        return null;
    }

    /**
     * <p>Returns the path to the file that contains the TLS client configuration parameters.</p><br />
     *
     * <p>The file is searched in the Domibus configuration location under a file named {@value #CLIENT_AUTHENTICATION_XML} by default. The first lookup happens by prefixing
     * this default name with the domain code followed by an underscore character. If the file having this name does not exist, another lookup happens by using just the default
     * name, without any prefixes. An optional path is returned when neither of these two files is found.</p>
     *
     * @param domainCode The domain code used to prefix the default file name during the first lookup.
     * @return the path to an existing domain specific client authentication file or the path to an existing default client authentication file; otherwise, an empty
     * {@code Optional} path.
     */
    private Optional<Path> getClientAuthenticationPath(String domainCode) {
        String domainSpecificFileName = StringUtils.stripToEmpty(domainCode) + "_" + CLIENT_AUTHENTICATION_XML;
        Path domainSpecificPath = Paths.get(domibusConfigurationService.getConfigLocation(), domainSpecificFileName);
        boolean domainSpecificPathExists = Files.exists(domainSpecificPath);
        LOG.debug("Client authentication file [{}] at the domain specific path [{}] exists [{}]", domainSpecificFileName, domainSpecificPath, domainSpecificPathExists);
        if(domainSpecificPathExists) {
            return Optional.of(domainSpecificPath);
        }

        Path defaultPath = Paths.get(domibusConfigurationService.getConfigLocation(), CLIENT_AUTHENTICATION_XML);
        boolean defaultPathExists = Files.exists(defaultPath);
        LOG.debug("Client authentication file at the default path [{}] exists [{}]", defaultPath, defaultPathExists);
        if(defaultPathExists) {
            return Optional.of(defaultPath);
        }

        return Optional.empty();
    }
}
