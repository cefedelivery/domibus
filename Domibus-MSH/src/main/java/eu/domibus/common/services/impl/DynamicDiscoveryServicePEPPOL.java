package eu.domibus.common.services.impl;

import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.services.DynamicDiscoveryService;
import eu.domibus.common.util.EndpointInfo;
import no.difi.vefa.peppol.common.lang.EndpointNotFoundException;
import no.difi.vefa.peppol.common.model.*;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.LookupClientBuilder;
import no.difi.vefa.peppol.lookup.api.LookupException;
import no.difi.vefa.peppol.lookup.fetcher.ApacheFetcher;
import no.difi.vefa.peppol.lookup.locator.BusdoxLocator;
import no.difi.vefa.peppol.security.Mode;
import no.difi.vefa.peppol.security.api.PeppolSecurityException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Properties;

/**
 * Service to query the SMP to extract the required information about the unknown receiver AP.
 * The SMP Lookup is done using an SMP Client software, with the following input:
 *       The End Receiver Participant ID (C4)
 *       The Document ID
 *       The Process ID
 *
 * Upon a successful lookup, the result contains the endpoint address and also othe public certificate of the receiver.
 */
@Service
@Qualifier("dynamicDiscoveryServicePEPPOL")
public class DynamicDiscoveryServicePEPPOL implements DynamicDiscoveryService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryServicePEPPOL.class);

    @Resource(name = "domibusProperties")
    private Properties domibusProperties;

    @Cacheable(value = "lookupInfo", key = "#receiverId + #receiverIdType + #documentId + #processId + #processIdType")
    public EndpointInfo lookupInformation(final String receiverId, final String receiverIdType, final String documentId, final String processId, final String processIdType) {

        LOG.info("[PEPPOL SMP] Do the lookup by: " + receiverId + " " + receiverIdType + " " + documentId + " " + processId + " " + processIdType);
        final String smlInfo = domibusProperties.getProperty(SMLZONE_KEY);
        if (smlInfo == null) {
            throw new ConfigurationException("SML Zone missing. Configure in domibus-configuration.xml");
        }
        String mode = domibusProperties.getProperty(DYNAMIC_DISCOVERY_MODE, Mode.TEST);
        final LookupClient smpClient = LookupClientBuilder.forMode(mode)
                .locator(new BusdoxLocator(smlInfo))
                .fetcher(new ApacheFetcher())
                .build();
        try {
            final ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(receiverId, new Scheme(receiverIdType));
            final DocumentTypeIdentifier documentIdentifier = new DocumentTypeIdentifier(documentId);

            final ProcessIdentifier processIdentifier = new ProcessIdentifier(processId, new Scheme(processIdType));
            LOG.debug("smpClient.getServiceMetadata");
            final ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            LOG.debug("sm.getEndpoint");
            final Endpoint endpoint;
            endpoint = sm.getEndpoint(processIdentifier, new TransportProfile(transportProfileAS4), TransportProfile.AS4);

            if (endpoint == null || endpoint.getAddress() == null || endpoint.getProcessIdentifier() == null) {
                throw new ConfigurationException("Receiver does not support reception of " + documentId + " for process " + processId + " using the AS4 Protocol");
            }

            return new EndpointInfo(endpoint.getAddress(), endpoint.getCertificate());

        } catch (final PeppolSecurityException | LookupException e) {
            throw new ConfigurationException("Receiver does not support reception of " + documentId + " for process " + processId + " using the AS4 Protocol", e);
        } catch (final EndpointNotFoundException e) {
            throw new ConfigurationException("Could not fetch metadata from SMP", e);
        }
    }
}