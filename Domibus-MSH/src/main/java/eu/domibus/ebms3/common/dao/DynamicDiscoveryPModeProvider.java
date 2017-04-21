package eu.domibus.ebms3.common.dao;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.pki.CertificateService;
import eu.domibus.wss4j.common.crypto.TrustStoreService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import eu.domibus.common.services.DynamicDiscoveryService;
import eu.domibus.common.services.impl.DynamicDiscoveryServiceOASIS;
import eu.domibus.common.services.impl.DynamicDiscoveryServicePEPPOL;
import eu.domibus.common.util.EndpointInfo;

import javax.naming.InvalidNameException;
import java.security.cert.X509Certificate;
import java.util.*;

/* This class is used for dynamic discovery of the parties participating in a message exchange.
 *
 * Dynamic discovery is activated when the pMode is configured with a dynamic
 * process (PMode.Initiator is not set and/or PMode.Responder is not set)
 *
 * The receiver of the message must be able to accept messages from previously unknown senders.
 * This requires the receiver to have one or more P-Modes configured for all registrations ii has in the SMP.
 * Therefore for each SMP Endpoint registration of the receiver with the type attribute set to 'bdxr-transport-ebms3-as4-v1p0'
 * there MUST exist a P-Mode that can handle a message with the following attributes:
 *      Service = ancestor::ServiceMetadata/ServiceInformation/Processlist/Process/ProcessIdentifier
 *      Service/@type = ancestor::ServiceMetadata/ServiceInformation/Processlist/Process/ProcessIdentifier/@scheme
 *      Action = ancestor::ServiceMetadata/ServiceInformation/DocumentIdentifier
 *
 * The sender must be able to send messages to unknown receivers. This requires that the sender performs a lookup to find
 * out the receivers details (partyId, type, endpoint address, public certificate - to encrypt the message).
 *
 * The sender may not register, it can send a message to a registered receiver even if he (the sender) is not registered.
 * Therefore, on the receiver there is no lookup for the sender. The message is accepted based on the root CA as long as the process matches.
 */
public class DynamicDiscoveryPModeProvider extends CachingPModeProvider {

    private static final String DYNAMIC_DISCOVERY_CLIENT_SPECIFICATION = "domibus.dynamic.discovery.client.specification";

    private static final Log LOG = LogFactory.getLog(DynamicDiscoveryPModeProvider.class);
    @Autowired
    protected TrustStoreService trustStoreService;
    @Autowired
    @Qualifier("domibusProperties")
    private java.util.Properties domibusProperties;

    @Autowired
    private DynamicDiscoveryServiceOASIS dynamicDiscoveryServiceOASIS;

    @Autowired
    private DynamicDiscoveryServicePEPPOL dynamicDiscoveryServicePEPPOL;

    protected DynamicDiscoveryService dynamicDiscoveryService = null;
    @Autowired
    protected CertificateService certificateService;
    protected Collection<eu.domibus.common.model.configuration.Process> dynamicResponderProcesses;
    protected Collection<eu.domibus.common.model.configuration.Process> dynamicInitiatorProcesses;

    // default type in e-SENS
    protected static final String URN_TYPE_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    protected static final String DEFAULT_RESPONDER_ROLE = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder";
    protected static final String MSH_ENDPOINT = "msh_endpoint";

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = IllegalStateException.class)
    public void init() {
        super.init();
        dynamicResponderProcesses = findDynamicResponderProcesses();
        dynamicInitiatorProcesses = findDynamicSenderProcesses();
        if(DynamicDiscoveryClientSpecification.PEPPOL.getName().equals(domibusProperties.getProperty(DYNAMIC_DISCOVERY_CLIENT_SPECIFICATION, "OASIS"))) {
            dynamicDiscoveryService = dynamicDiscoveryServicePEPPOL;
        } else { // OASIS client is used by default
            dynamicDiscoveryService = dynamicDiscoveryServiceOASIS;
        }
    }

    @Override
    public void refresh(){
        super.refresh();
        this.init();
    }

    protected Collection<eu.domibus.common.model.configuration.Process> findDynamicResponderProcesses() {
        final Collection<eu.domibus.common.model.configuration.Process> result = new ArrayList<>();
        for (final eu.domibus.common.model.configuration.Process process : this.getConfiguration().getBusinessProcesses().getProcesses()) {
            if (process.isDynamicResponder() && (process.isDynamicInitiator() || process.getInitiatorParties().contains(getConfiguration().getParty()))) {
                if (!process.getInitiatorParties().contains(getConfiguration().getParty())) {
                    process.getInitiatorParties().add(getConfiguration().getParty());
                }
                LOG.debug("Found dynamic receiver process: " + process.getName());
                result.add(process);
            }
        }
        return result;
    }

    protected Collection<eu.domibus.common.model.configuration.Process> findDynamicSenderProcesses() {
        final Collection<eu.domibus.common.model.configuration.Process> result = new ArrayList<>();
        for (final eu.domibus.common.model.configuration.Process process : this.getConfiguration().getBusinessProcesses().getProcesses()) {
            if (process.isDynamicInitiator() && (process.isDynamicResponder() || process.getResponderParties().contains(getConfiguration().getParty()))) {
                if (!process.getResponderParties().contains(getConfiguration().getParty())) {
                    process.getResponderParties().add(getConfiguration().getParty());
                }
                LOG.debug("Found dynamic sender process: " + process.getName());
                result.add(process);
            }
        }
        return result;
    }

    /* In case the static configuration doesn't match, update the
     * pMode using the dynamic discovery process and try again
     */
    @Override
    @Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = IllegalStateException.class)
    public String findPModeKeyForUserMessage(final UserMessage userMessage, final MSHRole mshRole) throws EbMS3Exception {
        try {
            return super.findPModeKeyForUserMessage(userMessage, mshRole);
        } catch (final EbMS3Exception e) {
            LOG.info("Start the dynamic discovery process", e);
            doDynamicDiscovery(userMessage, mshRole);

        }
        LOG.debug("Recalling findPModeKeyForUserMessage after the dynamic discovery");
        return super.findPModeKeyForUserMessage(userMessage, mshRole);
    }

    void doDynamicDiscovery(final UserMessage userMessage, final MSHRole mshRole) throws EbMS3Exception {
        Collection<eu.domibus.common.model.configuration.Process> candidates = findCandidateProcesses(userMessage, mshRole);

        if (candidates == null || candidates.isEmpty()) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "No matching dynamic discovery processes found for message.", userMessage.getMessageInfo().getMessageId(), null);
        }

        LOG.info("Found " + candidates.size() + " dynamic discovery candidates. MSHRole: " + mshRole);

        if(MSHRole.RECEIVING.equals(mshRole)) {
            PartyId fromPartyId = getFromPartyId(userMessage);
            Party configurationParty = updateConfigurationParty(fromPartyId.getValue(), fromPartyId.getType(), null);
            updateInitiatorPartiesInPmode(candidates, configurationParty);

        } else {//MSHRole.SENDING
            EndpointInfo endpointInfo = lookupByFinalRecipient(userMessage);
            updateToParty(userMessage, endpointInfo.getCertificate());
            PartyId toPartyId = getToPartyId(userMessage);
            Party configurationParty = updateConfigurationParty(toPartyId.getValue(), toPartyId.getType(), endpointInfo.getAddress());
            updateResponderPartiesInPmode(candidates, configurationParty);
        }
    }

    protected PartyId getToPartyId(UserMessage userMessage) throws EbMS3Exception {
        PartyId to = null;
        if(userMessage != null &&
                userMessage.getPartyInfo() != null &&
                userMessage.getPartyInfo().getTo() != null &&
                userMessage.getPartyInfo().getTo().getPartyId() != null &&
                userMessage.getPartyInfo().getTo().getPartyId().iterator() != null &&
                userMessage.getPartyInfo().getTo().getPartyId().iterator().hasNext()) {
            to = userMessage.getPartyInfo().getTo().getPartyId().iterator().next();
        }
        if (to == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Invalid To party identifier", null, null);
        }

        return to;
    }

    protected PartyId getFromPartyId(UserMessage userMessage) throws EbMS3Exception {
        PartyId from = null;
        if(userMessage != null &&
                userMessage.getPartyInfo() != null &&
                userMessage.getPartyInfo().getFrom() != null &&
                userMessage.getPartyInfo().getFrom().getPartyId() != null &&
                userMessage.getPartyInfo().getFrom().getPartyId().iterator() != null &&
                userMessage.getPartyInfo().getFrom().getPartyId().iterator().hasNext()) {
            from = userMessage.getPartyInfo().getFrom().getPartyId().iterator().next();
        }
        if (from == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Invalid From party identifier", null, null);
        }

        return from;
    }

    protected synchronized Party updateConfigurationParty(String name, String type, String endpoint) {
        LOG.info("Update the configuration party with: " + name + " " + type + " " + endpoint);
        // update the list of party types
        PartyIdType configurationType = updateConfigurationType(type);

        // search if the party exists in the pMode
        Party configurationParty = null;
        for (final Party party : getConfiguration().getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(party.getName(), name)) {
                LOG.debug("Party exists in the pmode: " + party.getName());
                configurationParty = party;
                break;
            }
        }

        // remove party if exists to add it with latest values for address and type
        if (configurationParty != null) {
            LOG.debug("Remove party to add with new values " + configurationParty.getName());
            getConfiguration().getBusinessProcesses().getParties().remove(configurationParty);
        }
        // set the new endpoint if exists, otherwise copy the old one if exists
        String newEndpoint = endpoint;
        if(newEndpoint == null) {
            newEndpoint = MSH_ENDPOINT;
            if (configurationParty != null && configurationParty.getEndpoint() != null) {
                newEndpoint = configurationParty.getEndpoint();
            }
        }

        LOG.debug("New endpoint is " + newEndpoint);
        Party newConfigurationParty = buildNewConfigurationParty(name, configurationType, newEndpoint);
        LOG.debug("Add new configuration party: " + newConfigurationParty.getName());
        getConfiguration().getBusinessProcesses().getParties().add(newConfigurationParty);

        return newConfigurationParty;
    }

    protected Party buildNewConfigurationParty(String name, PartyIdType configurationType, String endpoint) {
        Party newConfigurationParty = new Party();
        final Identifier partyIdentifier = new Identifier();
        partyIdentifier.setPartyId(name);
        partyIdentifier.setPartyIdType(configurationType);

        newConfigurationParty.setName(partyIdentifier.getPartyId());
        newConfigurationParty.getIdentifiers().add(partyIdentifier);
        newConfigurationParty.setEndpoint(endpoint);
        return newConfigurationParty;
    }

    protected PartyIdType updateConfigurationType(String type) {
        Set<PartyIdType> partyIdTypes = getConfiguration().getBusinessProcesses().getPartyIdTypes();
        if (partyIdTypes == null) {
            LOG.info("Empty partyIdTypes set");
            partyIdTypes = new HashSet<>();
        }

        PartyIdType configurationType = null;
        for (final PartyIdType t : partyIdTypes) {
            if (StringUtils.equalsIgnoreCase(t.getValue(), type)) {
                LOG.debug("PartyIdType exists in the pmode: " + type);
                configurationType = t;
            }
        }
        // add to partyIdType list
        if (configurationType == null) {
            LOG.debug("Add new PartyIdType: " + type);
            configurationType = new PartyIdType();
            configurationType.setName(type);
            configurationType.setValue(type);
            partyIdTypes.add(configurationType);
            this.getConfiguration().getBusinessProcesses().setPartyIdTypes(partyIdTypes);
        }
        return configurationType;
    }

    protected synchronized void updateResponderPartiesInPmode(Collection<eu.domibus.common.model.configuration.Process> candidates, Party configurationParty) {
        LOG.debug("updateResponderPartiesInPmode with party " + configurationParty.getName());
        for (final Process candidate : candidates) {
            boolean partyFound = false;
            for (final Party party : candidate.getResponderParties()) {
                if (StringUtils.equalsIgnoreCase(configurationParty.getName(), party.getName())) {
                    partyFound = true;
                    LOG.debug("partyFound in candidate: " + candidate.getName());
                    break;
                }
            }
            if (!partyFound) {
                candidate.getResponderParties().add(configurationParty);
            }
        }
    }

    protected synchronized void updateInitiatorPartiesInPmode(Collection<eu.domibus.common.model.configuration.Process> candidates, Party configurationParty) {
        LOG.debug("updateInitiatorPartiesInPmode with party " + configurationParty.getName());
        for (final Process candidate : candidates) {
            boolean partyFound = false;
            for (final Party party : candidate.getInitiatorParties()) {
                if (StringUtils.equalsIgnoreCase(configurationParty.getName(), party.getName())) {
                    partyFound = true;
                    LOG.debug("partyFound in candidate: " + candidate.getName());
                    break;
                }
            }
            if (!partyFound) {
                candidate.getInitiatorParties().add(configurationParty);
            }
        }
    }

    protected void updateToParty(UserMessage userMessage, final X509Certificate certificate) throws EbMS3Exception{
        String cn = null;
        try {
            //parse certificate for common name = toPartyId
            cn = certificateService.extractCommonName(certificate);
            LOG.debug("Extracted the common name: " + cn);
        } catch (final InvalidNameException e) {
            LOG.error("Error while extracting CommonName from certificate", e);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "Error while extracting CommonName from certificate", null, e);
        }
        //set toPartyId in UserMessage
        final PartyId receiverParty = new PartyId();
        receiverParty.setValue(cn);
        receiverParty.setType(URN_TYPE_VALUE);

        userMessage.getPartyInfo().getTo().getPartyId().clear();
        userMessage.getPartyInfo().getTo().getPartyId().add(receiverParty);
        if(userMessage.getPartyInfo().getTo().getRole() == null) {
            userMessage.getPartyInfo().getTo().setRole(DEFAULT_RESPONDER_ROLE);
        }

        LOG.debug("Add public certificate to the truststore");
        //add certificate to Truststore
        trustStoreService.addCertificate(certificate, cn, true);
        LOG.debug("Certificate added");

    }

    protected EndpointInfo lookupByFinalRecipient(UserMessage userMessage) throws EbMS3Exception {
        Property finalRecipient = getFinalRecipient(userMessage);
        if(finalRecipient == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Dynamic discovery processes found for message but finalRecipient information is missing in messageProperties.", userMessage.getMessageInfo().getMessageId(), null);
        }
        LOG.info("Perform lookup by finalRecipient: " + finalRecipient.getName() + " " + finalRecipient.getType() + " " +finalRecipient.getValue());

        //lookup sml/smp - result is cached
        final EndpointInfo endpoint = dynamicDiscoveryService.lookupInformation(finalRecipient.getValue(),
                finalRecipient.getType(),
                userMessage.getCollaborationInfo().getAction(),
                userMessage.getCollaborationInfo().getService().getValue(),
                userMessage.getCollaborationInfo().getService().getType());

        LOG.debug("Lookup successful: " + endpoint.getAddress());
        return endpoint;
    }

    /*
     * Check all dynamic processes to find candidates for dynamic discovery lookup.
     */
    protected Collection<eu.domibus.common.model.configuration.Process> findCandidateProcesses(UserMessage userMessage, final MSHRole mshRole) {
        Collection<eu.domibus.common.model.configuration.Process> candidates = new HashSet<>();
        Collection<eu.domibus.common.model.configuration.Process> processes = MSHRole.SENDING.equals(mshRole) ? dynamicResponderProcesses : dynamicInitiatorProcesses;

        for (final Process process : processes) {
            if (matchProcess(process, mshRole)) {
                LOG.debug("Process matched: " + process.getName() + "  " + mshRole);
                for (final LegConfiguration legConfiguration : process.getLegs()) {
                    if (StringUtils.equalsIgnoreCase(legConfiguration.getService().getValue(), userMessage.getCollaborationInfo().getService().getValue()) &&
                            StringUtils.equalsIgnoreCase(legConfiguration.getAction().getValue(), userMessage.getCollaborationInfo().getAction())) {
                        LOG.debug("Leg matched, adding process. Leg: " + legConfiguration.getName());
                        candidates.add(process);
                    }
                }
            }
        }

        return candidates;
    }

    /*
     * On the receiving, the initiator is unknown, on the sending side the responder is unknown.
     */
    protected boolean matchProcess(final Process process, MSHRole mshRole) {
        if(MSHRole.RECEIVING.equals(mshRole)) {
            return process.isDynamicInitiator() || process.getInitiatorParties().contains(this.getConfiguration().getParty());
        } else { // MSHRole.SENDING
            return process.isDynamicResponder() || process.getResponderParties().contains(this.getConfiguration().getParty());
        }
    }

    protected Property getFinalRecipient(UserMessage userMessage) {
        if(userMessage.getMessageProperties() == null ||
                userMessage.getMessageProperties().getProperty().isEmpty()) {
            LOG.warn("Empty property set");
            return null;
        }

        for (final eu.domibus.ebms3.common.model.Property p : userMessage.getMessageProperties().getProperty()) {
            if (p.getName() != null && p.getName().equals(MessageConstants.FINAL_RECIPIENT)) {
                return p;
            }
            LOG.debug("Property: " + p.getName());
        }
        return null;
    }
}
