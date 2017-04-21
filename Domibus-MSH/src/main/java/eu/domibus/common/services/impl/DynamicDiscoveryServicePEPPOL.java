package eu.domibus.common.services.impl;

import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.services.DynamicDiscoveryService;
import eu.domibus.common.util.EndpointInfo;
import no.difi.vefa.edelivery.lookup.LookupClient;
import no.difi.vefa.edelivery.lookup.LookupClientBuilder;
import no.difi.vefa.edelivery.lookup.api.LookupException;
import no.difi.vefa.edelivery.lookup.locator.BusdoxLocator;
import no.difi.vefa.edelivery.lookup.model.DocumentIdentifier;
import no.difi.vefa.edelivery.lookup.model.Endpoint;
import no.difi.vefa.edelivery.lookup.model.ParticipantIdentifier;
import no.difi.vefa.edelivery.lookup.model.ProcessIdentifier;
import no.difi.vefa.edelivery.lookup.model.ServiceMetadata;
import no.difi.vefa.edelivery.lookup.model.TransportProfile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class DynamicDiscoveryServicePEPPOL implements DynamicDiscoveryService {

    private static final Log LOG = LogFactory.getLog(DynamicDiscoveryServicePEPPOL.class);

    @Resource(name = "domibusProperties")
    private Properties domibusProperties;

    @Cacheable(value = "lookupInfo", key = "#receiverId + #receiverIdType + #documentId + #processId + #processIdType")
    public EndpointInfo lookupInformation(final String receiverId, final String receiverIdType, final String documentId, final String processId, final String processIdType) {

        LOG.info("[PEPPOL SMP] Do the lookup by: " + receiverId + " " + receiverIdType + " " + documentId + " " + processId + " " + processIdType);
        final String smlInfo = domibusProperties.getProperty(SMLZONE_KEY);
        if (smlInfo == null) {
            throw new ConfigurationException("SML Zone missing. Configure in domibus-configuration.xml");
        }
        final LookupClient smpClient = LookupClientBuilder.newInstance()
                .locator(new BusdoxLocator(smlInfo))
                .build();
        try {
            final ParticipantIdentifier participantIdentifier = new ParticipantIdentifier(receiverId, receiverIdType);
            final DocumentIdentifier documentIdentifier = new DocumentIdentifier(documentId);

            final ProcessIdentifier processIdentifier = new ProcessIdentifier(processId, processIdType);
            LOG.debug("smpClient.getServiceMetadata");
            final ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            LOG.debug("sm.getEndpoint");
            final Endpoint endpoint;
            endpoint = sm.getEndpoint(processIdentifier, new TransportProfile(transportProfileAS4), TransportProfile.AS4);

            if (endpoint == null || endpoint.getAddress() == null || endpoint.getProcessIdentifier() == null) {
                throw new ConfigurationException("Receiver does not support reception of " + documentId + " for process " + processId + " using the AS4 Protocol");
            }

            return new EndpointInfo(endpoint.getAddress(), endpoint.getCertificate());

        } catch (final LookupException e) {
            throw new ConfigurationException("Receiver does not support reception of " + documentId + " for process " + processId + " using the AS4 Protocol", e);
        } catch (final no.difi.vefa.edelivery.lookup.api.SecurityException e) {
            throw new ConfigurationException("Could not fetch metadata from SMP", e);
        }
    }
}