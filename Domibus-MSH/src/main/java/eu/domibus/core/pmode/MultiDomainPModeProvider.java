package eu.domibus.core.pmode;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.util.xml.UnmarshallerResult;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.context.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.AgreementRef;
import eu.domibus.ebms3.common.model.PartyId;
import eu.domibus.ebms3.common.model.Service;
import eu.domibus.ebms3.common.model.UserMessage;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@org.springframework.stereotype.Service
@Primary
public class MultiDomainPModeProvider extends PModeProvider {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(MultiDomainPModeProvider.class);

    protected volatile Map<Domain, PModeProvider> providerMap = new HashMap<>();

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected PModeProviderFactoryImpl pModeProviderFactory;

    @Override
    public void init() {
        //nothing to initialize
    }

    @Override
    public void refresh() {
        getCurrentPModeProvider().refresh();
    }

    protected PModeProvider getCurrentPModeProvider() {
        final Domain currentDomain = domainContextProvider.getCurrentDomain();
        LOG.debug("Get domain PMode provider for domain [{}]", currentDomain);
        PModeProvider pModeProvider = providerMap.get(currentDomain);
        if (pModeProvider == null) {
            synchronized (providerMap) {
                // retrieve again from map, otherwise it is null even for the second thread(because the variable has method scope)
                pModeProvider = providerMap.get(currentDomain);
                if (pModeProvider == null) { //NOSONAR: double-check locking
                    LOG.debug("Creating domain PMode provider  for domain [{}]", currentDomain);
                    pModeProvider = pModeProviderFactory.createDomainPModeProvider(currentDomain);
                    providerMap.put(currentDomain, pModeProvider);
                }
            }
        }
        return pModeProvider;
    }

    @Override
    public boolean isConfigurationLoaded() {
        return getCurrentPModeProvider().isConfigurationLoaded();
    }

    @Override
    public List<String> getMpcList() {
        return getCurrentPModeProvider().getMpcList();
    }

    @Override
    public List<String> getMpcURIList() {
        return getCurrentPModeProvider().getMpcURIList();
    }

    @Override
    protected String findLegName(String agreementRef, String senderParty, String receiverParty, String service, String action) throws EbMS3Exception {
        return getCurrentPModeProvider().findLegName(agreementRef, senderParty, receiverParty, service, action);
    }

    @Override
    protected String findActionName(String action) throws EbMS3Exception {
        return getCurrentPModeProvider().findActionName(action);
    }

    @Override
    protected String findServiceName(Service service) throws EbMS3Exception {
        return getCurrentPModeProvider().findServiceName(service);
    }

    @Override
    protected String findPartyName(Collection<PartyId> partyId) throws EbMS3Exception {
        return getCurrentPModeProvider().findPartyName(partyId);
    }


    @Override
    public MessageExchangeConfiguration findUserMessageExchangeContext(final UserMessage userMessage, final MSHRole mshRole) throws EbMS3Exception {
        return getCurrentPModeProvider().findUserMessageExchangeContext(userMessage, mshRole);
    }

    @Override
    protected String findAgreement(AgreementRef agreementRef) throws EbMS3Exception {
        return getCurrentPModeProvider().findAgreement(agreementRef);
    }

    @Override
    public Party getGatewayParty() {
        return getCurrentPModeProvider().getGatewayParty();
    }

    @Override
    public Party getSenderParty(String pModeKey) {
        return getCurrentPModeProvider().getSenderParty(pModeKey);
    }

    @Override
    public Party getReceiverParty(String pModeKey) {
        return getCurrentPModeProvider().getReceiverParty(pModeKey);
    }

    @Override
    public Party getPartyByIdentifier(String partyIdentifier) {
        return getCurrentPModeProvider().getPartyByIdentifier(partyIdentifier);
    }

    @Override
    public eu.domibus.common.model.configuration.Service getService(String pModeKey) {
        return getCurrentPModeProvider().getService(pModeKey);
    }

    @Override
    public Action getAction(String pModeKey) {
        return getCurrentPModeProvider().getAction(pModeKey);
    }

    @Override
    public Agreement getAgreement(String pModeKey) {
        return getCurrentPModeProvider().getAgreement(pModeKey);
    }

    @Override
    public LegConfiguration getLegConfiguration(String pModeKey) {
        return getCurrentPModeProvider().getLegConfiguration(pModeKey);
    }

    @Override
    public boolean isMpcExistant(String mpc) {
        return getCurrentPModeProvider().isMpcExistant(mpc);
    }

    @Override
    public int getRetentionDownloadedByMpcName(String mpcName) {
        return getCurrentPModeProvider().getRetentionDownloadedByMpcName(mpcName);
    }

    @Override
    public int getRetentionDownloadedByMpcURI(String mpcURI) {
        return getCurrentPModeProvider().getRetentionDownloadedByMpcURI(mpcURI);
    }

    @Override
    public int getRetentionUndownloadedByMpcName(String mpcName) {
        return getCurrentPModeProvider().getRetentionUndownloadedByMpcName(mpcName);
    }

    @Override
    public int getRetentionUndownloadedByMpcURI(String mpcURI) {
        return getCurrentPModeProvider().getRetentionUndownloadedByMpcURI(mpcURI);
    }

    @Override
    public Role getBusinessProcessRole(String roleValue) {
        return getCurrentPModeProvider().getBusinessProcessRole(roleValue);
    }

    @Override
    public List<Process> findPullProcessesByMessageContext(MessageExchangeConfiguration messageExchangeConfiguration) {
        return getCurrentPModeProvider().findPullProcessesByMessageContext(messageExchangeConfiguration);
    }

    @Override
    public List<Process> findPullProcessesByInitiator(Party party) {
        return getCurrentPModeProvider().findPullProcessesByInitiator(party);
    }

    @Override
    public List<Process> findPullProcessByMpc(String mpc) {
        return getCurrentPModeProvider().findPullProcessByMpc(mpc);
    }

    @Override
    public List<Process> findAllProcesses() {
        return getCurrentPModeProvider().findAllProcesses();
    }

    @Override
    public List<Party> findAllParties() {
        return getCurrentPModeProvider().findAllParties();
    }

    @Override
    public List<String> findPartyIdByServiceAndAction(String service, String action) {
        return getCurrentPModeProvider().findPartyIdByServiceAndAction(service, action);
    }

    @Override
    public String getPartyIdType(String partyIdentifier) {
        return getCurrentPModeProvider().getPartyIdType(partyIdentifier);
    }

    @Override
    public String getServiceType(String serviceValue) {
        return getCurrentPModeProvider().getServiceType(serviceValue);
    }

    @Override
    public String getRole(String roleType, String serviceValue) {
        return getCurrentPModeProvider().getRole(roleType, serviceValue);
    }

    @Override
    public String getAgreementRef(String serviceValue) {
        return getCurrentPModeProvider().getAgreementRef(serviceValue);
    }

    @Override
    public byte[] getPModeFile(int id) {
        return getCurrentPModeProvider().getPModeFile(id);
    }

    @Override
    public ConfigurationRaw getRawConfiguration(int id) {
        return getCurrentPModeProvider().getRawConfiguration(id);
    }

    @Override
    public PModeArchiveInfo getCurrentPmode() {
        return getCurrentPModeProvider().getCurrentPmode();
    }

    @Override
    public void removePMode(int id) {
        getCurrentPModeProvider().removePMode(id);
    }

    @Override
    public List<PModeArchiveInfo> getRawConfigurationList() {
        return getCurrentPModeProvider().getRawConfigurationList();
    }

    @Override
    protected UnmarshallerResult parsePMode(byte[] bytes) throws XmlProcessingException {
        return getCurrentPModeProvider().parsePMode(bytes);
    }

    @Override
    public Configuration getPModeConfiguration(byte[] bytes) throws XmlProcessingException {
        return getCurrentPModeProvider().getPModeConfiguration(bytes);
    }

    @Override
    public List<String> updatePModes(byte[] bytes, String description) throws XmlProcessingException {
        return getCurrentPModeProvider().updatePModes(bytes, description);
    }

    @Override
    protected UnmarshallerResult unmarshall(byte[] bytes, boolean ignoreWhitespaces) throws XmlProcessingException {
        return getCurrentPModeProvider().unmarshall(bytes, ignoreWhitespaces);
    }

    @Override
    public byte[] serializePModeConfiguration(Configuration configuration) throws XmlProcessingException {
        return getCurrentPModeProvider().serializePModeConfiguration(configuration);
    }

    @Override
    protected String getSenderPartyNameFromPModeKey(String pModeKey) {
        return getCurrentPModeProvider().getSenderPartyNameFromPModeKey(pModeKey);
    }

    @Override
    protected String getReceiverPartyNameFromPModeKey(String pModeKey) {
        return getCurrentPModeProvider().getReceiverPartyNameFromPModeKey(pModeKey);
    }

    @Override
    protected String getServiceNameFromPModeKey(String pModeKey) {
        return getCurrentPModeProvider().getServiceNameFromPModeKey(pModeKey);
    }

    @Override
    protected String getActionNameFromPModeKey(String pModeKey) {
        return getCurrentPModeProvider().getActionNameFromPModeKey(pModeKey);
    }

    @Override
    protected String getAgreementRefNameFromPModeKey(String pModeKey) {
        return getCurrentPModeProvider().getAgreementRefNameFromPModeKey(pModeKey);
    }

    @Override
    protected String getLegConfigurationNameFromPModeKey(String pModeKey) {
        return getCurrentPModeProvider().getLegConfigurationNameFromPModeKey(pModeKey);
    }
}
