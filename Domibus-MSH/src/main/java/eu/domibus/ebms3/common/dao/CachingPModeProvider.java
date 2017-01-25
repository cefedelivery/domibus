package eu.domibus.ebms3.common.dao;

import eu.domibus.common.ErrorCode;
import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.model.AgreementRef;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.lang.StringUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.springframework.util.StringUtils.hasLength;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class CachingPModeProvider extends PModeProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CachingPModeProvider.class);

    //Dont access directly, use getter instead
    private Configuration configuration;

    protected synchronized Configuration getConfiguration() {
        if (this.configuration == null) {
            this.init();
        }
        return this.configuration;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = IllegalStateException.class)
    public void init() {
        if (!this.configurationDAO.configurationExists()) {
            throw new IllegalStateException("No processing modes found. To exchange messages, upload configuration file through the web gui.");
        }
        this.configuration = this.configurationDAO.readEager();
    }


    @Override
    //FIXME: only works for the first leg, as sender=initiator
    protected String findLegName(final String agreementName, final String senderParty, final String receiverParty, final String service, final String action) throws EbMS3Exception {
        final List<LegConfiguration> candidates = new ArrayList<>();
        for (final Process process : this.getConfiguration().getBusinessProcesses().getProcesses()) {
            for (final Party party : process.getInitiatorParties()) {
                if (StringUtils.equalsIgnoreCase(party.getName(), senderParty)) {
                    for (final Party responder : process.getResponderParties()) {
                        if (StringUtils.equalsIgnoreCase(responder.getName(), receiverParty)) {
                            if (process.getAgreement() != null && StringUtils.equalsIgnoreCase(process.getAgreement().getName(), agreementName)
                                    || (agreementName.equals(OPTIONAL_AND_EMPTY) && process.getAgreement() == null)
                                    // Please notice that this is only for backward compatibility and will be removed ASAP!
                                    || (agreementName.equals(OPTIONAL_AND_EMPTY) && process.getAgreement() != null && process.getAgreement().getValue().equals(""))
                                    ) {
                                /**
                                 * The Process is a candidate because either has an Agreement and its name matches the Agreement name found previously
                                 * or it has no Agreement configured and the Agreement name was not indicated in the submitted message.
                                 **/
                                candidates.addAll(process.getLegs());
                            }
                        }
                    }
                }
            }
        }
        if (candidates.isEmpty()) {
            LOG.businessError(DomibusMessageCode.BUS_LEG_NAME_NOT_FOUND, agreementName, senderParty, receiverParty, service, action);
            throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No Candidates for Legs found", null, null);
        }
        for (final LegConfiguration candidate : candidates) {
            if (StringUtils.equalsIgnoreCase(candidate.getService().getName(), service) && StringUtils.equalsIgnoreCase(candidate.getAction().getName(), action)) {
                return candidate.getName();
            }
        }
        LOG.businessError(DomibusMessageCode.BUS_LEG_NAME_NOT_FOUND, agreementName, senderParty, receiverParty, service, action);
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching leg found", null, null);
    }

    @Override
    protected String findActionName(final String action) throws EbMS3Exception {
        for (final Action action1 : this.getConfiguration().getBusinessProcesses().getActions()) {
            if (StringUtils.equalsIgnoreCase(action1.getValue(), action)) {
                return action1.getName();
            }
        }
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching action found", null, null);
    }

    @Override
    protected String findServiceName(final eu.domibus.ebms3.common.model.Service service) throws EbMS3Exception {
        for (final Service service1 : this.getConfiguration().getBusinessProcesses().getServices()) {
            if ((StringUtils.equalsIgnoreCase(service1.getServiceType(), service.getType()) || (!hasLength(service1.getServiceType()) && !hasLength(service.getType()))))
                if (StringUtils.equalsIgnoreCase(service1.getValue(), service.getValue())) {
                    return service1.getName();
                }
        }
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching service found", null, null);
    }

    @Override
    //@Transactional(propagation = Propagation.SUPPORTS, noRollbackFor = IllegalStateException.class)
    protected String findPartyName(final Collection<PartyId> partyId) throws EbMS3Exception {
        String partyIdType = "";
        for (final Party party : this.getConfiguration().getBusinessProcesses().getParties()) {
            for (final PartyId id : partyId) {
                for (final Identifier identifier : party.getIdentifiers()) {
                    if (id.getType() != null) {
                        partyIdType = id.getType();
                        try {
                            URI.create(partyIdType);
                        } catch (final IllegalArgumentException e) {
                            final EbMS3Exception ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "no matching party found", null, e);
                            ex.setErrorDetail("PartyId " + id.getValue() + " is not a valid URI [CORE] 5.2.2.3");
                            throw ex;
                        }
                    }
                    String identifierPartyIdType = "";
                    if (identifier.getPartyIdType() != null) {
                        identifierPartyIdType = identifier.getPartyIdType().getValue();
                    }

                    if (StringUtils.equalsIgnoreCase(partyIdType, identifierPartyIdType) && StringUtils.equalsIgnoreCase(id.getValue(), identifier.getPartyId())) {
                        return party.getName();
                    }
                }
            }
        }
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "No matching party found", null, null);
    }

    @Override
    protected String findAgreement(final AgreementRef agreementRef) throws EbMS3Exception {
        if (agreementRef == null || agreementRef.getValue() == null || agreementRef.getValue().isEmpty()) {
            return OPTIONAL_AND_EMPTY; // AgreementRef is optional
        }

        for (final Agreement agreement : this.getConfiguration().getBusinessProcesses().getAgreements()) {
            if ((StringUtils.isEmpty(agreementRef.getType()) || StringUtils.equalsIgnoreCase(agreement.getType(), agreementRef.getType()))
                    && StringUtils.equalsIgnoreCase(agreementRef.getValue(), agreement.getValue())) {
                return agreement.getName();
            }
        }
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0001, "No matching agreement found", null, null);
    }

    @Override
    public Party getSenderParty(final String pModeKey) {
        final String partyKey = this.getSenderPartyNameFromPModeKey(pModeKey);
        for (final Party party : this.getConfiguration().getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(party.getName(), partyKey)) {
                return party;
            }
        }
        throw new ConfigurationException("no matching sender party found with name: " + partyKey);
    }

    @Override
    public Party getReceiverParty(final String pModeKey) {
        final String partyKey = this.getReceiverPartyNameFromPModeKey(pModeKey);
        for (final Party party : this.getConfiguration().getBusinessProcesses().getParties()) {
            if (StringUtils.equalsIgnoreCase(party.getName(), partyKey)) {
                return party;
            }
        }
        throw new ConfigurationException("no matching receiver party found with name: " + partyKey);
    }

    @Override
    public Service getService(final String pModeKey) {
        final String serviceKey = this.getServiceNameFromPModeKey(pModeKey);
        for (final Service service : this.getConfiguration().getBusinessProcesses().getServices()) {
            if (StringUtils.equalsIgnoreCase(service.getName(), serviceKey)) {
                return service;
            }
        }
        throw new ConfigurationException("no matching service found with name: " + serviceKey);
    }

    @Override
    public Action getAction(final String pModeKey) {
        final String actionKey = this.getActionNameFromPModeKey(pModeKey);
        for (final Action action : this.getConfiguration().getBusinessProcesses().getActions()) {
            if (StringUtils.equalsIgnoreCase(action.getName(), actionKey)) {
                return action;
            }
        }
        throw new ConfigurationException("no matching action found with name: " + actionKey);
    }

    @Override
    public Agreement getAgreement(final String pModeKey) {
        final String agreementKey = this.getAgreementRefNameFromPModeKey(pModeKey);
        for (final Agreement agreement : this.getConfiguration().getBusinessProcesses().getAgreements()) {
            if (StringUtils.equalsIgnoreCase(agreement.getName(), agreementKey)) {
                return agreement;
            }
        }
        throw new ConfigurationException("no matching agreement found with name: " + agreementKey);
    }

    @Override
    public LegConfiguration getLegConfiguration(final String pModeKey) {
        final String legKey = this.getLegConfigurationNameFromPModeKey(pModeKey);
        for (final LegConfiguration legConfiguration : this.getConfiguration().getBusinessProcesses().getLegConfigurations()) {
            if (StringUtils.equalsIgnoreCase(legConfiguration.getName(), legKey)) {
                return legConfiguration;
            }
        }
        throw new ConfigurationException("no matching legConfiguration found with name: " + legKey);
    }

    @Override
    public boolean isMpcExistant(final String mpc) {
        for (final Mpc mpc1 : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc1.getName(), mpc)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getRetentionDownloadedByMpcName(final String mpcName) {
        for (final Mpc mpc1 : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc1.getName(), mpcName)) {
                return mpc1.getRetentionDownloaded();
            }
        }

        CachingPModeProvider.LOG.error("No MPC with name: " + mpcName + " found. Assuming message retention of 0 for downloaded messages.");

        return 0;
    }

    @Override
    public int getRetentionDownloadedByMpcURI(final String mpcURI) {
        for (final Mpc mpc1 : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc1.getQualifiedName(), mpcURI)) {
                return mpc1.getRetentionDownloaded();
            }
        }

        CachingPModeProvider.LOG.error("No MPC with name: " + mpcURI + " found. Assuming message retention of 0 for downloaded messages.");

        return 0;
    }

    @Override
    public int getRetentionUndownloadedByMpcName(final String mpcName) {
        for (final Mpc mpc1 : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc1.getName(), mpcName)) {
                return mpc1.getRetentionUndownloaded();
            }
        }

        CachingPModeProvider.LOG.error("No MPC with name: " + mpcName + " found. Assuming message retention of -1 for undownloaded messages.");

        return -1;
    }

    @Override
    public int getRetentionUndownloadedByMpcURI(final String mpcURI) {
        for (final Mpc mpc1 : this.getConfiguration().getMpcs()) {
            if (StringUtils.equalsIgnoreCase(mpc1.getQualifiedName(), mpcURI)) {
                return mpc1.getRetentionUndownloaded();
            }
        }

        CachingPModeProvider.LOG.error("No MPC with name: " + mpcURI + " found. Assuming message retention of -1 for undownloaded messages.");

        return -1;
    }

    @Override
    public List<String> getMpcList() {
        final List<String> result = new ArrayList<>();
        for (final Mpc mpc : this.getConfiguration().getMpcs()) {
            result.add(mpc.getName());
        }
        return result;
    }

    @Override
    public List<String> getMpcURIList() {
        final List<String> result = new ArrayList<>();
        for (final Mpc mpc : this.getConfiguration().getMpcs()) {
            result.add(mpc.getQualifiedName());
        }
        return result;
    }

    @Override
    public Role getBusinessProcessRole(String roleValue) throws EbMS3Exception {
        for (Role role : this.getConfiguration().getBusinessProcesses().getRoles()) {
            if (StringUtils.equalsIgnoreCase(role.getValue(), roleValue)) {
                return role;
            }
        }
        LOG.businessError(DomibusMessageCode.BUS_PARTY_ROLE_NOT_FOUND, roleValue);
        //throw new ConfigurationException("No matching Role found with value [" + roleValue + "]");
        throw new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0003, "No matching Role found with value [" + roleValue + "]", null, null);
    }

    @Override
    public void refresh() {
        this.configuration = null;
        this.getConfiguration(); //reloads the config
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<String> updatePModes(final byte[] bytes) throws XmlProcessingException {
        List<String> messages = super.updatePModes(bytes);
        this.configuration = null;
        return messages;
    }
}
