package eu.domibus.common.services.impl;

import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.services.DynamicDiscoveryService;
import eu.domibus.common.util.DomibusApacheFetcher;
import eu.domibus.common.util.EndpointInfo;
import eu.domibus.api.util.HttpUtil;
import no.difi.vefa.peppol.common.lang.EndpointNotFoundException;
import no.difi.vefa.peppol.common.lang.PeppolLoadingException;
import no.difi.vefa.peppol.common.lang.PeppolParsingException;
import no.difi.vefa.peppol.common.model.*;
import no.difi.vefa.peppol.lookup.locator.StaticLocator;
import no.difi.vefa.peppol.mode.*;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.LookupClientBuilder;
import no.difi.vefa.peppol.lookup.api.LookupException;
import no.difi.vefa.peppol.lookup.locator.BusdoxLocator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;
import no.difi.vefa.peppol.security.util.EmptyCertificateValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String RESPONDER_ROLE = "urn:fdc:peppol.eu:2017:roles:ap:as4";
    private static final String PARTYID_TYPE = "urn:fdc:peppol.eu:2017:identifiers:ap";

    @Resource(name = "domibusProperties")
    private Properties domibusProperties;

    @Autowired
    HttpUtil httpUtil;

    @Cacheable(value = "lookupInfo", key = "#receiverId + #receiverIdType + #documentId + #processId + #processIdType")
    public EndpointInfo lookupInformation(final String receiverId, final String receiverIdType, final String documentId, final String processId, final String processIdType) {

        LOG.info("[PEPPOL SMP] Do the lookup by: " + receiverId + " " + receiverIdType + " " + documentId + " " + processId + " " + processIdType);
        final String smlInfo = domibusProperties.getProperty(SMLZONE_KEY);
        if (smlInfo == null) {
            throw new ConfigurationException("SML Zone missing. Configure in domibus-configuration.xml");
        }
        String mode = domibusProperties.getProperty(DYNAMIC_DISCOVERY_MODE, Mode.TEST);
        try {
            final LookupClient smpClient = LookupClientBuilder.forMode(mode)
                    .locator(new BusdoxLocator(smlInfo))
//                    .locator(new StaticLocator("https://my-json-server.typicode.com"))
                    .fetcher(new DomibusApacheFetcher(Mode.of(mode), httpUtil))
                    .certificateValidator(EmptyCertificateValidator.INSTANCE)
                    .build();

            final ParticipantIdentifier participantIdentifier = ParticipantIdentifier.of(receiverId, Scheme.of(receiverIdType));
            final DocumentTypeIdentifier documentIdentifier = DocumentTypeIdentifier.of(documentId);

            final ProcessIdentifier processIdentifier = ProcessIdentifier.parse(processId);
            LOG.debug("Getting the ServiceMetadata");
            final ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            LOG.debug("Getting the Endpoint from ServiceMetadata");
            final Endpoint endpoint = sm.getEndpoint(processIdentifier, TransportProfile.AS4);

            if (endpoint == null || endpoint.getAddress() == null) {
                throw new ConfigurationException("Could not fetch metadata from SMP for documentId " + documentId + " processId " + processId);
            }
            return new EndpointInfo(endpoint.getAddress().toString(), endpoint.getCertificate());
        } catch (final PeppolParsingException | PeppolLoadingException | PeppolSecurityException | LookupException | EndpointNotFoundException | IllegalStateException e) {
            throw new ConfigurationException("Could not fetch metadata from SMP for documentId " + documentId + " processId " + processId, e);
        }
    }

    @Override
    public String getPartyIdType() {
        return domibusProperties.getProperty(DYNAMIC_DISCOVERY_PARTYID_TYPE, PARTYID_TYPE);
    }

    @Override
    public String getResponderRole(){
        return domibusProperties.getProperty(DYNAMIC_DISCOVERY_PARTYID_RESPONDER_ROLE, RESPONDER_ROLE);
    }

}