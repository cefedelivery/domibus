package eu.domibus.ebms3.common.dao;

import com.google.gson.GsonBuilder;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.model.*;
import eu.domibus.ebms3.common.model.Property;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.wss4j.common.crypto.TrustStoreService;
import no.difi.vefa.edelivery.lookup.model.Endpoint;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/* Dynamic Discovery of the corresponding receiving AP for responder parties not configured in the pMode
 *
 *
*/

public class DynamicDiscoveryPModeProvider extends CachingPModeProvider {

    private static final Log LOG = LogFactory.getLog(DynamicDiscoveryPModeProvider.class);
    @Autowired
    protected TrustStoreService trustStoreService;
    @Autowired
    private DynamicDiscoveryService dynamicDiscoveryService;
    protected Collection<eu.domibus.common.model.configuration.Process> dynamicReceiverProcesses;
    protected Collection<eu.domibus.common.model.configuration.Process> dynamicInitiatorProcesses;

    protected static final String URN_TYPE_VALUE = "urn:oasis:names:tc:ebcore:partyid-type:unregistered";
    protected static final String URN_TYPE_NAME ="partyTypeUrn";

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = IllegalStateException.class)
    public void init() {
        super.init();
        dynamicReceiverProcesses = findDynamicReceiverProcesses();
        dynamicInitiatorProcesses = findDynamicSenderProcesses();
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

    Collection<eu.domibus.common.model.configuration.Process> findDynamicSenderProcesses() {
        final Collection<eu.domibus.common.model.configuration.Process> result = new ArrayList<eu.domibus.common.model.configuration.Process>();
        for (final eu.domibus.common.model.configuration.Process process : this.getConfiguration().getBusinessProcesses().getProcesses()) {
            if (process.isDynamicInitiator() && (process.isDynamicResponder() || process.getResponderParties().contains(getConfiguration().getParty()))) {
                if (!process.getResponderParties().contains(getConfiguration().getParty())) {
                    process.getResponderParties().add(getConfiguration().getParty());
                }
                result.add(process);
            }
        }
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = IllegalStateException.class)
    public String findPModeKeyForUserMessage(final UserMessage userMessage, final MSHRole mshRole) throws EbMS3Exception {
        try {
            return super.findPModeKeyForUserMessage(userMessage, mshRole);
        } catch (final EbMS3Exception e) {
            LOG.debug("Do dynamic: ", e);
            doDynamicDiscovery(userMessage, mshRole);

        }
        return super.findPModeKeyForUserMessage(userMessage, mshRole);
    }

    void doDynamicDiscovery(final UserMessage userMessage, final MSHRole mshRole) throws EbMS3Exception {
        Collection<eu.domibus.common.model.configuration.Process> candidates = findCandidateProcesses(userMessage, mshRole);

        if (candidates.isEmpty()) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "No matching dynamic discovery processes found for message.", userMessage.getMessageInfo().getMessageId(), null);
        }

        if(MSHRole.RECEIVING.equals(mshRole)) {
            PartyId fromPartyId = userMessage.getPartyInfo().getFrom().getPartyId().iterator().next();
            Party configurationParty = buildConfigurationParty(fromPartyId.getValue(), fromPartyId.getType(), null);
            updateInitiatorPartiesInPmode(candidates, configurationParty);

        } else {//MSHRole.SENDING
            Endpoint endpoint = lookupByFinalRecipient(userMessage);
            updateToParty(userMessage, endpoint);
            PartyId toPartyId = userMessage.getPartyInfo().getTo().getPartyId().iterator().next();
            Party configurationParty = buildConfigurationParty(toPartyId.getValue(), null, endpoint.getAddress());
            updateResponderPartiesInPmode(candidates, configurationParty);
        }

        //TODO move this to debug
        String dump = new GsonBuilder().setPrettyPrinting().create().toJson(getConfiguration());
        LOG.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  PMODE  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        LOG.info(dump);
        LOG.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  END PMODE  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

    }

    protected Party buildConfigurationParty(String name, String type, String endpoint) {

        Set<PartyIdType> partyIdTypes = getConfiguration().getBusinessProcesses().getPartyIdTypes();
        if (partyIdTypes == null) {
            partyIdTypes = new HashSet<>();
        }

        String typeName = null;
        String typeValue = type;
        if(type == null) {
            typeName = URN_TYPE_NAME;
            typeValue = URN_TYPE_VALUE;
        }

        PartyIdType configurationType = null;
        for (final PartyIdType t : partyIdTypes) {
            if (StringUtils.equalsIgnoreCase(t.getValue(), typeValue)
                    && (typeName != null && StringUtils.equalsIgnoreCase(t.getName(), typeName))) {
                configurationType = t;
            }
        }
        // add to partyIdType list
        if(configurationType == null) {
            configurationType = new PartyIdType();
            configurationType.setName(typeName);
            configurationType.setValue(typeValue != null ? typeValue : typeName);
            partyIdTypes.add(configurationType);
            this.getConfiguration().getBusinessProcesses().setPartyIdTypes(partyIdTypes);
        }

        Party configurationParty = null;
        for (final Party party : getConfiguration().getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(party.getName(), name)) {
                configurationParty = party;
                break;
            }
        }

        // remove party and add it with latest values for address and type
        if(configurationParty != null) {
            getConfiguration().getBusinessProcesses().getParties().remove(configurationParty);
        }

        final Identifier partyIdentifier = new Identifier();
        partyIdentifier.setPartyId(name);
        partyIdentifier.setPartyIdType(configurationType);
        Party newConfigurationParty = new Party();
        newConfigurationParty.setName(partyIdentifier.getPartyId());
        newConfigurationParty.getIdentifiers().add(partyIdentifier);
        // hack
        newConfigurationParty.setEndpoint("http://localhost:8180/domibus/services/msh");//endpoint.getAddress());
        //if(endpoint != null) {
            //configurationParty.setEndpoint(endpoint.getAddress());
        //}

        return newConfigurationParty;
    }

    protected void updateResponderPartiesInPmode(Collection<eu.domibus.common.model.configuration.Process> candidates, Party configurationParty) {

        this.getConfiguration().getBusinessProcesses().getParties().add(configurationParty);

        for (final Process candidate : candidates) {
            boolean partyFound = false;
            for (final Party party : candidate.getResponderParties()) {
                if (StringUtils.equalsIgnoreCase(configurationParty.getName(), party.getName())) {
                    partyFound = true;
                    break;
                }
            }
            if (!partyFound) {
                candidate.getResponderParties().add(configurationParty);
            }
        }
    }

    protected void updateInitiatorPartiesInPmode(Collection<eu.domibus.common.model.configuration.Process> candidates, Party configurationParty) {

        this.getConfiguration().getBusinessProcesses().getParties().add(configurationParty);

        for (final Process candidate : candidates) {
            boolean partyFound = false;
            for (final Party party : candidate.getInitiatorParties()) {
                if (StringUtils.equalsIgnoreCase(configurationParty.getName(), party.getName())) {
                    partyFound = true;
                    break;
                }
            }
            if (!partyFound) {
                candidate.getInitiatorParties().add(configurationParty);
            }
        }
    }

    protected void updateToParty(UserMessage userMessage, final Endpoint endpoint) {

        String cn = null;
        try {
            //parse certificate for common name = toPartyId
            cn = extractCommonName(endpoint.getCertificate());
        } catch (final InvalidNameException e) {
            LOG.error("Error while extracting CommonName from certificate", e);
        }
        // hack
        cn = "red_gw";

        //set toPartyId in UserMessage
        final PartyId receiverParty = new PartyId();
        receiverParty.setValue(cn);
        receiverParty.setType(URN_TYPE_VALUE);

        userMessage.getPartyInfo().getTo().getPartyId().clear();
        userMessage.getPartyInfo().getTo().getPartyId().add(receiverParty);

        // hack
        X509Certificate red_cert = getRedCert();

        //add certificate to Truststore
        //trustStoreService.addCertificate(endpoint.getCertificate(), cn, true);
        trustStoreService.addCertificate(red_cert, cn, true);
    }

    protected Endpoint lookupByFinalRecipient(UserMessage userMessage) throws EbMS3Exception {
        Property finalRecipient = getFinalRecipient(userMessage);
        if(finalRecipient == null) {
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0010, "Dynamic discovery processes found for message but finalRecipient information is missing in messageProperties.", userMessage.getMessageInfo().getMessageId(), null);
        }
        LOG.info("finalRecipient: " + finalRecipient.getName() + " " + finalRecipient.getType() + " " +finalRecipient.getValue());

        //lookup sml/smp
        final Endpoint endpoint = dynamicDiscoveryService.lookupInformation(finalRecipient.getValue(),
                finalRecipient.getType(),
                userMessage.getCollaborationInfo().getAction(),
                userMessage.getCollaborationInfo().getService().getValue(),
                userMessage.getCollaborationInfo().getService().getType());

        return endpoint;
    }


    protected Collection<eu.domibus.common.model.configuration.Process> findCandidateProcesses(UserMessage userMessage, final MSHRole mshRole) {
        Collection<eu.domibus.common.model.configuration.Process> candidates = new HashSet<>();
        Collection<eu.domibus.common.model.configuration.Process> processes = MSHRole.SENDING.equals(mshRole) ? dynamicReceiverProcesses : dynamicInitiatorProcesses;

        for (final Process process : processes) {
            if (matchProcess(process, mshRole)) {
                for (final LegConfiguration legConfiguration : process.getLegs()) {
                    if (StringUtils.equalsIgnoreCase(legConfiguration.getService().getValue(), userMessage.getCollaborationInfo().getService().getValue()) &&
                            StringUtils.equalsIgnoreCase(legConfiguration.getAction().getValue(), userMessage.getCollaborationInfo().getAction())) {
                        candidates.add(process);
                    }
                }
            }
        }
        return candidates;
    }

    protected boolean matchProcess(final Process process, MSHRole mshRole) {
        if(MSHRole.RECEIVING.equals(mshRole)) {
            return process.isDynamicInitiator() || process.getInitiatorParties().contains(this.getConfiguration().getParty());
        } else { // MSHRole.SENDING
            return process.isDynamicResponder() || process.getResponderParties().contains(this.getConfiguration().getParty());
        }
    }

    protected String extractCommonName(final X509Certificate certificate) throws InvalidNameException {

        final String dn = certificate.getSubjectDN().getName();
        LOG.debug("DN is: " + dn);
        final LdapName ln = new LdapName(dn);
        for (final Rdn rdn : ln.getRdns()) {
            if (StringUtils.equalsIgnoreCase(rdn.getType(), "CN")) {
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

    protected Property getFinalRecipient(UserMessage userMessage) {
        if(userMessage.getMessageProperties() == null ||
                userMessage.getMessageProperties().getProperty().isEmpty()) {
            return null;
        }

        for (final eu.domibus.ebms3.common.model.Property p : userMessage.getMessageProperties().getProperty()) {
            if (p.getName() != null && p.getName().equals(MessageConstants.FINAL_RECIPIENT)) {
                return p;
            }
        }
        return null;
    }

    //hack
    private X509Certificate getRedCert() {

        try {
            final KeyStore red_ks = KeyStore.getInstance(KeyStore.getDefaultType());
            String red_filename = "/Users/idragusa/_setup/3.2.2/domibus-MSH-3.2-SNAPSHOT-tomcat-full/domibus_c3/conf/domibus/keystores_sample/gateway_truststore.jks";
            char[] red_password = "test123".toCharArray();
            red_ks.load(new FileInputStream(red_filename), red_password);
            KeyStore.TrustedCertificateEntry keyEnt = (KeyStore.TrustedCertificateEntry)red_ks.getEntry("red_gw", null);
            X509Certificate red_cert = (X509Certificate)keyEnt.getTrustedCertificate();
            return red_cert;
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableEntryException e) {
            return null;
        }
     }
}
