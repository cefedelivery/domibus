package eu.domibus.ebms3.common.dao;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.AgreementRef;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.common.model.Service;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@org.springframework.stereotype.Service
public class MultiDomainPModeProvider extends PModeProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiDomainPModeProvider.class);

    protected volatile Map<Domain, PModeProvider> providerMap = new HashMap<>();

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Override
    public void init() {
        //nothing to initialize
    }

    @Override
    public void refresh() {


    }

    protected PModeProvider getCurrentPModeProvider() {
        final Domain currentDomain = domainContextProvider.getCurrentDomain();
        final PModeProvider pModeProvider = providerMap.get(currentDomain);
        if(pModeProvider ) {

        }
    }

    @Override
    public boolean isConfigurationLoaded() {
        return false;
    }

    @Override
    public List<String> getMpcList() {
        return null;
    }

    @Override
    public List<String> getMpcURIList() {
        return null;
    }

    @Override
    protected String findLegName(String agreementRef, String senderParty, String receiverParty, String service, String action) throws EbMS3Exception {
        return null;
    }

    @Override
    protected String findActionName(String action) throws EbMS3Exception {
        return null;
    }

    @Override
    protected String findServiceName(Service service) throws EbMS3Exception {
        return null;
    }

    @Override
    protected String findPartyName(Collection<PartyId> partyId) throws EbMS3Exception {
        return null;
    }

    @Override
    protected String findAgreement(AgreementRef agreementRef) throws EbMS3Exception {
        return null;
    }

    @Override
    public Party getGatewayParty() {
        return null;
    }

    @Override
    public Party getSenderParty(String pModeKey) {
        return null;
    }

    @Override
    public Party getReceiverParty(String pModeKey) {
        return null;
    }

    @Override
    public eu.domibus.common.model.configuration.Service getService(String pModeKey) {
        return null;
    }

    @Override
    public Action getAction(String pModeKey) {
        return null;
    }

    @Override
    public Agreement getAgreement(String pModeKey) {
        return null;
    }

    @Override
    public LegConfiguration getLegConfiguration(String pModeKey) {
        return null;
    }

    @Override
    public boolean isMpcExistant(String mpc) {
        return false;
    }

    @Override
    public int getRetentionDownloadedByMpcName(String mpcName) {
        return 0;
    }

    @Override
    public int getRetentionDownloadedByMpcURI(String mpcURI) {
        return 0;
    }

    @Override
    public int getRetentionUndownloadedByMpcName(String mpcName) {
        return 0;
    }

    @Override
    public int getRetentionUndownloadedByMpcURI(String mpcURI) {
        return 0;
    }

    @Override
    public Role getBusinessProcessRole(String roleValue) {
        return null;
    }

    @Override
    public List<Process> findPullProcessesByMessageContext(MessageExchangeConfiguration messageExchangeConfiguration) {
        return null;
    }

    @Override
    public List<Process> findPullProcessesByInitiator(Party party) {
        return null;
    }

    @Override
    public List<Process> findPullProcessByMpc(String mpc) {
        return null;
    }

    @Override
    public List<Process> findAllProcesses() {
        return null;
    }

    @Override
    public List<Party> findAllParties() {
        return null;
    }

    @Override
    public List<String> findPartyIdByServiceAndAction(String service, String action) {
        return null;
    }

    @Override
    public String getPartyIdType(String partyIdentifier) {
        return null;
    }

    @Override
    public String getServiceType(String serviceValue) {
        return null;
    }

    @Override
    public String getRole(String roleType, String serviceValue) {
        return null;
    }

    @Override
    public String getAgreementRef(String serviceValue) {
        return null;
    }
}
