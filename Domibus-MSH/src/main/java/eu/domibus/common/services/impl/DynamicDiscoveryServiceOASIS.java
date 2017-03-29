package eu.domibus.common.services.impl;

import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.services.DynamicDiscoveryService;
import eu.domibus.common.util.EndpointInfo;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.europa.ec.dynamicdiscovery.DynamicDiscovery;
import eu.europa.ec.dynamicdiscovery.DynamicDiscoveryBuilder;
import eu.europa.ec.dynamicdiscovery.core.locator.impl.DefaultBDXRLocator;
import eu.europa.ec.dynamicdiscovery.core.reader.impl.DefaultBDXRReader;
import eu.europa.ec.dynamicdiscovery.core.security.impl.DefaultSignatureValidator;
import eu.europa.ec.dynamicdiscovery.exception.TechnicalException;
import eu.europa.ec.dynamicdiscovery.model.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * Service to query a compliant e-SENS SMP profile based on the OASIS BDX Service Metadata Publishers (SMP) to extract the required information about the unknown receiver AP.
 * The SMP Lookup is done using an SMP Client software, with the following input:
 *       The End Receiver Participant ID (C4)
 *       The Document ID
 *       The Process ID
 *
 * Upon a successful lookup, the result contains the endpoint address and also othe public certificate of the receiver.
 */
@Service
public class DynamicDiscoveryServiceOASIS implements DynamicDiscoveryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryServiceOASIS.class);

    @Resource(name = "domibusProperties")
    private Properties domibusProperties;

    @Cacheable(value = "lookupInfo", key = "#receiverId + #receiverIdType + #documentId + #processId + #processIdType")
    public EndpointInfo lookupInformation(final String receiverId, final String receiverIdType, final String documentId, final String processId, final String processIdType) {

        LOG.info("[OASIS SMP] Do the lookup by: " + receiverId + " " + receiverIdType + " " + documentId + " " + processId + " " + processIdType);
        final String smlInfo = domibusProperties.getProperty(SMLZONE_KEY);
        if (smlInfo == null) {
            throw new ConfigurationException("SML Zone missing. Configure in domibus-configuration.xml");
        }

        KeyStore truststore = null;
        try {
            truststore = KeyStore.getInstance("JKS");
            truststore.load(new FileInputStream(new File("/Users/idragusa/_setup/dyn_disc_mixed_with_static/truststoreForTrustedCertificate.ts")), "test".toCharArray());
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException exc ) {
            throw new ConfigurationException("Could not fetch metadata from SMP", exc);
        }

        try {
            // TODO add proxy to fetcher
            DynamicDiscovery smpClient = DynamicDiscoveryBuilder.newInstance()
                    .locator(new DefaultBDXRLocator("ehealth.acc.edelivery.tech.ec.europa.eu"))
                    .reader(new DefaultBDXRReader(new DefaultSignatureValidator(truststore)))
                    .build();

            final ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(receiverId, receiverIdType);
            final DocumentIdentifier documentIdentifier = new DocumentIdentifier(documentId, "");

            final ProcessIdentifier processIdentifier = new ProcessIdentifier(processId, processIdType);

            ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);

            final Endpoint endpoint;
            endpoint = sm.getEndpoint(processIdentifier, new TransportProfile(transportProfileAS4));

            if (endpoint == null || endpoint.getAddress() == null || endpoint.getProcessIdentifier() == null) {
                throw new ConfigurationException("Receiver does not support reception of " + documentId + " for process " + processId + " using the AS4 Protocol");
            }

            return new EndpointInfo(endpoint.getAddress(), endpoint.getCertificate());

        } catch (TechnicalException exc) {
            throw new ConfigurationException("Could not fetch metadata from SMP", exc);
        }
    }

    protected String proxyInfo() {
        String proxyStr = "";
        String httpProxyHost = domibusProperties.getProperty("domibus.proxy.http.host");
        String httpProxyPort = domibusProperties.getProperty("domibus.proxy.http.port");
        String httpProxyUser = domibusProperties.getProperty("domibus.proxy.user");
        String httpProxyPassword = domibusProperties.getProperty("domibus.proxy.password");
        String httpNonProxyHosts = domibusProperties.getProperty("domibus.proxy.nonProxyHosts");

        return proxyStr;
    }
}
