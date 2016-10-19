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

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.Identifier;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.wss4j.common.crypto.CryptoService;
import no.difi.vefa.edelivery.lookup.model.Endpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Proof of concept and not production ready!
 *
 * @author Christian Koch, Stefan Mueller
 */
public class DynamicDiscoveryPModeProvider extends CachingPModeProvider {

    private static final Log LOG = LogFactory.getLog(DynamicDiscoveryPModeProvider.class);
    @Autowired
    protected CryptoService cryptoService;
    @Autowired
    private DynamicDiscoveryService dynamicDiscoveryService;
    protected Collection<eu.domibus.common.model.configuration.Process> dynamicReceiverProcesses;

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = IllegalStateException.class)
    public void init() {
        super.init();
        dynamicReceiverProcesses = findDynamicReceiverProcesses();
    }

    Collection<eu.domibus.common.model.configuration.Process> findDynamicReceiverProcesses() {
        final Collection<eu.domibus.common.model.configuration.Process> result = new ArrayList<eu.domibus.common.model.configuration.Process>();
        for (final eu.domibus.common.model.configuration.Process process : this.getConfiguration().getBusinessProcesses().getProcesses()) {
            if (process.isDynamicResponder() && (process.isDynamicInitiator() || process.getInitiatorParties().contains(getConfiguration().getParty()))) {
                if (!process.getInitiatorParties().contains(getConfiguration().getParty())) {
                    process.getInitiatorParties().add(getConfiguration().getParty());
                }
                result.add(process);
            }
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = IllegalStateException.class)
    public String findPModeKeyForUserMessage(final UserMessage userMessage) throws EbMS3Exception {

        try {
            return super.findPModeKeyForUserMessage(userMessage);
        } catch (final EbMS3Exception e) {
            //do dynamic things
            LOG.debug("Do dynamic: ", e);
            doDynamicThings(userMessage);

        }


        return super.findPModeKeyForUserMessage(userMessage);
    }

    void doDynamicThings(final UserMessage userMessage) throws EbMS3Exception {
        final Collection<eu.domibus.common.model.configuration.Process> candidates = new HashSet<Process>();
        for (final Process process : this.dynamicReceiverProcesses) {
            if (process.isDynamicInitiator() || process.getInitiatorParties().contains(this.getConfiguration().getParty())) {
                for (final LegConfiguration legConfiguration : process.getLegs()) {
                    if (legConfiguration.getService().getValue().equals(userMessage.getCollaborationInfo().getService().getValue()) &&
                            legConfiguration.getAction().getValue().equals(userMessage.getCollaborationInfo().getAction())) {
                        candidates.add(process);
                    }
                }
            }
        }
        if (candidates.isEmpty()) {

            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "no matching dynamic discovery processes found for message.", userMessage.getMessageInfo().getMessageId(), null);
        }

        final PartyId partyId = userMessage.getPartyInfo().getTo().getPartyId().iterator().next();
        //lookup sml/smp
        final Endpoint endpoint = dynamicDiscoveryService.lookupInformation(partyId.getValue(),
                partyId.getType(),
                userMessage.getCollaborationInfo().getAction(),
                userMessage.getCollaborationInfo().getService().getValue(),
                userMessage.getCollaborationInfo().getService().getType());


        String cn = null;
        try {
            //parse certificate for common name = toPartyId
            cn = extractCommonName(endpoint.getCertificate());
        } catch (final InvalidNameException e) {
            LOG.error("Error while extracting CommonName from certificate", e);
        }


        //set toPartyId in UserMessage
        final PartyId receiverParty = new PartyId();
        receiverParty.setValue(cn);

        userMessage.getPartyInfo().getTo().getPartyId().add(receiverParty);

        //add certificate to Truststore
        cryptoService.addCertificate(endpoint.getCertificate(), cn, true);

        //check if party is available in cache
        Party configurationToParty = null;
        for (final Party party : getConfiguration().getBusinessProcesses().getParties()) {
            if (party.getName().equals(cn)) {
                configurationToParty = party;
            }
        }

        //if party is not already there, create a new one and add it to configuration
        if (configurationToParty == null) {
            configurationToParty = new Party();
            configurationToParty.setName(cn);
            final Identifier toPartyIdentifier = new Identifier();
            //PartyIdType is empty
            toPartyIdentifier.setPartyId(cn);
            configurationToParty.getIdentifiers().add(toPartyIdentifier);
            configurationToParty.setEndpoint(endpoint.getAddress());
            this.getConfiguration().getBusinessProcesses().getParties().add(configurationToParty);
        }

        for (final Process candidate : candidates) {
            boolean partyFound = false;
            for (final Party party : candidate.getResponderParties()) {
                if (configurationToParty.getName().equals(party.getName())) {
                    partyFound = true;
                    break;
                }
            }
            if (!partyFound) {
                candidate.getResponderParties().add(configurationToParty);
            }
        }
    }

    String extractCommonName(final X509Certificate certificate) throws InvalidNameException {

        final String dn = certificate.getSubjectDN().getName();
        LOG.debug("DN is: " + dn);
        final LdapName ln = new LdapName(dn);
        for (final Rdn rdn : ln.getRdns()) {
            if (rdn.getType().equalsIgnoreCase("CN")) {
                LOG.debug("CN is: " + rdn.getValue());
                return rdn.getValue().toString();
            }
        }

        throw new IllegalArgumentException("The certificate does not contain a common name (CN): " + certificate.getSubjectDN().getName());

    }
    @Override
    public void refresh(){
        super.refresh();
        this.init();
    }
}
