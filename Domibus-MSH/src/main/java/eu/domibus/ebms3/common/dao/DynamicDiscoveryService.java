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

package eu.domibus.ebms3.common.dao;

import eu.domibus.common.exception.ConfigurationException;
import no.difi.vefa.edelivery.lookup.LookupClient;
import no.difi.vefa.edelivery.lookup.LookupClientBuilder;
import no.difi.vefa.edelivery.lookup.api.LookupException;
import no.difi.vefa.edelivery.lookup.locator.BusdoxLocator;
import no.difi.vefa.edelivery.lookup.model.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
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
public class DynamicDiscoveryService {

    public static final String SMLZONE_KEY = "domibus.smlzone";
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryService.class);
    protected static final String transportProfileDynDisc = "bdxr-transport-ebms3-as4-v1p0";
    @Resource(name = "domibusProperties")
    private Properties domibusProperties;

    @Cacheable(value = "lookupInfo", key = "#receiverId + #receiverIdType + #documentId + #processId + #processIdType")
    public Endpoint lookupInformation(final String receiverId, final String receiverIdType, final String documentId, final String processId, final String processIdType) {

        LOG.info("Do the lookup by: " + receiverId + " " + receiverIdType + " " + documentId + " " + processId + " " + processIdType);
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
            endpoint = sm.getEndpoint(processIdentifier, new TransportProfile(transportProfileDynDisc), TransportProfile.AS4);

            if (endpoint == null || endpoint.getAddress() == null || endpoint.getProcessIdentifier() == null) {
                throw new ConfigurationException("Receiver does not support reception of " + documentId + " for process " + processId + " using the AS4 Protocol");
            }
            return endpoint;

        } catch (final LookupException e) {
            throw new ConfigurationException("Receiver does not support reception of " + documentId + " for process " + processId + " using the AS4 Protocol", e);
        } catch (final no.difi.vefa.edelivery.lookup.api.SecurityException e) {
            throw new ConfigurationException("Could not fetch metadata from SMP", e);
        }
    }
}
