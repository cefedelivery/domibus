package eu.domibus.common.services.impl;

import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.services.DynamicDiscoveryService;
import eu.domibus.common.util.EndpointInfo;
import eu.domibus.wss4j.common.crypto.CryptoService;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.fetcher.impl.DefaultURLFetcher;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultProxy;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.exception.ConnectionException;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.KeyStore;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to query a compliant e-SENS SMP profile based on the OASIS BDX Service Metadata Publishers
 * (SMP) to extract the required information about the unknown receiver AP.
 * The SMP Lookup is done using an SMP Client software, with the following input:
 *       The End Receiver Participant ID (C4)
 *       The Document ID
 *       The Process ID
 *
 * Upon a successful lookup, the result contains the endpoint address and also othe public
 * certificate of the receiver.
 */
@Service
@Qualifier("dynamicDiscoveryServiceOASIS")
public class DynamicDiscoveryServiceOASIS implements DynamicDiscoveryService {

    private static final Log LOG = LogFactory.getLog(DynamicDiscoveryServiceOASIS.class);
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^(?<scheme>.+?)::(?<value>.+)$");

    @Resource(name = "domibusProperties")
    private Properties domibusProperties;

    @Autowired
    private CryptoService cryptoService;

    @Cacheable(value = "lookupInfo", key = "#receiverId + #receiverIdType + #documentId + #processId + #processIdType")
    public EndpointInfo lookupInformation(final String receiverId, final String receiverIdType,
                                          final String documentId, final String processId,
                                          final String processIdType) {

        LOG.info("[OASIS SMP] Do the lookup by: " + receiverId + " " + receiverIdType + " " + documentId +
                " " + processId + " " + processIdType);

        try {
            DynamicDiscovery smpClient = createDynamicDiscoveryClient();

            LOG.debug("Preparing to request the ServiceMetadata");
            final ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(receiverId, receiverIdType);
            final DocumentIdentifier documentIdentifier = createDocumentIdentifier(documentId);
            final ProcessIdentifier processIdentifier = new ProcessIdentifier(processId, processIdType);
            ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);

            LOG.debug("Get the endpoint for " + transportProfileAS4);
            final Endpoint endpoint = sm.getEndpoint(processIdentifier, new TransportProfile(transportProfileAS4));
            if (endpoint == null || endpoint.getAddress() == null || endpoint.getProcessIdentifier() == null) {
                throw new ConfigurationException("Receiver does not support reception of " + documentId +
                        " for process " + processId + " using the AS4 Protocol");
            }

            return new EndpointInfo(endpoint.getAddress(), endpoint.getCertificate());

        } catch (TechnicalException exc) {
            throw new ConfigurationException("Could not fetch metadata from SMP", exc);
        }
    }

    protected DynamicDiscovery createDynamicDiscoveryClient() {
        final String smlInfo = domibusProperties.getProperty(SMLZONE_KEY);
        if (smlInfo == null) {
            throw new ConfigurationException("SML Zone missing. Configure in domibus-configuration.xml");
        }

        LOG.debug("Load trustore for the smpClient");
        KeyStore truststore = cryptoService.getTrustStore();
        try {
            DefaultProxy defaultProxy = getConfiguredProxy();
            if (defaultProxy != null) {
                LOG.debug("Creating SMP client with proxy");
                return DynamicDiscoveryBuilder.newInstance()
                        .fetcher(new DefaultURLFetcher(defaultProxy))
                        .locator(new DefaultBDXRLocator(smlInfo))
                        .reader(new DefaultBDXRReader(new DefaultSignatureValidator(truststore)))
                        .build();
            }

            LOG.debug("Creating SMP client without proxy");
            // no proxy is configured
            return DynamicDiscoveryBuilder.newInstance()
                    .locator(new DefaultBDXRLocator(smlInfo))
                    .reader(new DefaultBDXRReader(new DefaultSignatureValidator(truststore)))
                    .build();

        } catch (TechnicalException exc) {
            throw new ConfigurationException("Could not create smp client to fetch metadata from SMP", exc);
        }
    }

    protected DocumentIdentifier createDocumentIdentifier(String documentId) {
        String scheme = extract(documentId, "scheme");
        String value = extract(documentId, "value");
        return new DocumentIdentifier(value, scheme);
    }

    protected String extract(String doubleColonDelimitedId, String groupName) {
        Matcher m = IDENTIFIER_PATTERN.matcher(doubleColonDelimitedId);
        m.matches();
        return m.group(groupName);
    }

    protected DefaultProxy getConfiguredProxy() throws ConnectionException {
        String httpProxyHost = domibusProperties.getProperty("domibus.proxy.http.host");
        String httpProxyPort = domibusProperties.getProperty("domibus.proxy.http.port");
        String httpProxyUser = domibusProperties.getProperty("domibus.proxy.user");
        String httpProxyPassword = domibusProperties.getProperty("domibus.proxy.password");

        if(StringUtils.isEmpty(httpProxyHost) || StringUtils.isEmpty(httpProxyPort)) {
            return null;
        }

        LOG.info("Proxy configured: " + httpProxyHost + " " + httpProxyPort + " " +
                httpProxyUser + " " + httpProxyPassword + " ");

        return new DefaultProxy(httpProxyHost, Integer.parseInt(httpProxyPort), httpProxyUser, httpProxyPassword);
    }
}