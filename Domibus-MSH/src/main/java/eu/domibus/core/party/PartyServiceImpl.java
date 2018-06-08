package eu.domibus.core.party;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.party.Party;
import eu.domibus.api.party.PartyService;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.common.dao.PartyDao;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.pki.CertificateService;
import java.security.cert.X509Certificate;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class PartyServiceImpl implements PartyService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyServiceImpl.class);
    private static final Predicate<Party> DEFAULT_PREDICATE = condition -> true;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private PartyDao partyDao;

    @Autowired
    protected MultiDomainCryptoService multiDomainCertificateProvider;

    @Autowired
    protected DomainContextProvider domainProvider;

    @Autowired
    private CertificateService certificateService;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Party> getParties(final String name,
                                  final String endPoint,
                                  final String partyId,
                                  final String processName,
                                  final int pageStart,
                                  final int pageSize) {

        final Predicate<Party> searchPredicate = getSearchPredicate(name, endPoint, partyId, processName);
        return linkPartyAndProcesses().
                stream().
                filter(searchPredicate).
                skip(pageStart).
                limit(pageSize).
                collect(Collectors.toList());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long countParties(String name, String endPoint, String partyId, String processName) {
        final Predicate<Party> searchPredicate = getSearchPredicate(name, endPoint, partyId, processName);
        return linkPartyAndProcesses().
                stream().
                filter(searchPredicate).
                count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> findPartyNamesByServiceAndAction(String service, String action) {
        return pModeProvider.findPartyIdByServiceAndAction(service, action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGatewayPartyIdentifier() {
        String result = null;
        eu.domibus.common.model.configuration.Party gatewayParty = pModeProvider.getGatewayParty();
        // return the first identifier
        if (!gatewayParty.getIdentifiers().isEmpty()) {
            result = gatewayParty.getIdentifiers().iterator().next().getPartyId();
        }
        return result;
    }

    /**
     * In the actual configuration the link between parties and processes exists from process to party.
     * We need to reverse this association, we want to have a relation party -> process I am involved in as a responder
     * or initiator.
     *
     * @return a list of party linked with their processes.
     */
    protected List<Party> linkPartyAndProcesses() {

        //Retrieve all party entities.
        List<eu.domibus.common.model.configuration.Party> allParties = pModeProvider.findAllParties();
        if (LOG.isDebugEnabled()) {
            LOG.debug("linkPartyAndProcesses for party entities");
            allParties.forEach(party -> LOG.debug("     [{}]", party));
        }

        //create a new Party to live outside the service per existing party entity in the pmode.
        List<Party> parties = domainCoreConverter.convert(allParties, Party.class);

        //transform parties to map for convenience.
        final Map<String, Party> partyMapByName =
                parties.
                        stream().
                        collect(collectingAndThen(toMap(Party::getName, Function.identity()), ImmutableMap::copyOf));

        //retrieve all existing processes in the pmode.
        final List<Process> allProcesses =
                pModeProvider.findAllProcesses().
                        stream().
                        collect(collectingAndThen(toList(), ImmutableList::copyOf));

        linkProcessWithPartyAsInitiator(partyMapByName, allProcesses);

        linkProcessWithPartyAsResponder(partyMapByName, allProcesses);

        if (LOG.isDebugEnabled()) {
            LOG.debug("     party");
            parties.forEach(party -> LOG.debug("[{}]", party));
        }

        return parties;
    }


    protected void linkProcessWithPartyAsInitiator(final Map<String, Party> partyMapByName, final List<Process> allProcesses) {
        allProcesses.forEach(
                processEntity -> {
                    //loop process initiators.
                    processEntity.getInitiatorParties().forEach(partyEntity -> {
                                Party party = partyMapByName.get(partyEntity.getName());
                                eu.domibus.api.process.Process process = domainCoreConverter.convert(
                                        processEntity,
                                        eu.domibus.api.process.Process.class);
                                //add the processes for which this party is initiator.
                                party.addProcessesWithPartyAsInitiator(process);
                            }
                    );
                }
        );
    }

    protected void linkProcessWithPartyAsResponder(final Map<String, Party> partyMapByName, final List<Process> allProcesses) {
        allProcesses.forEach(
                processEntity -> {
                    //loop process responder.
                    processEntity.getResponderParties().forEach(partyEntity -> {
                                Party party = partyMapByName.get(partyEntity.getName());
                                eu.domibus.api.process.Process process = domainCoreConverter.convert(
                                        processEntity,
                                        eu.domibus.api.process.Process.class);
                                //add the processes for which this party is responder.
                                party.addprocessesWithPartyAsResponder(process);
                            }
                    );
                }
        );
    }

    protected Predicate<Party> getSearchPredicate(String name, String endPoint, String partyId, String processName) {
        return namePredicate(name).
                and(endPointPredicate(endPoint)).
                and(partyIdPredicate(partyId)).
                and(processPredicate(processName));
    }

    protected Predicate<Party> namePredicate(final String name) {

        if (StringUtils.isNotEmpty(name)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("create name predicate for [{}]", name);
            }
            return
                    party ->
                            StringUtils.containsIgnoreCase(party.getName(), name.toUpperCase());

        }
        return DEFAULT_PREDICATE;

    }

    protected Predicate<Party> endPointPredicate(final String endPoint) {
        if (StringUtils.isNotEmpty(endPoint)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("create endPoint predicate for [{}]", endPoint);
            }
            return party ->
                    StringUtils.containsIgnoreCase(party.getEndpoint(), endPoint.toUpperCase());
        }
        return DEFAULT_PREDICATE;
    }

    protected Predicate<Party> partyIdPredicate(final String partyId) {
        //Search in the list of partyId to find one that match the search criteria.
        if (StringUtils.isNotEmpty(partyId)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("create partyId predicate for [{}]", partyId);
            }
            return
                    party -> {
                        long count = party.getIdentifiers().stream().
                                filter(identifier -> StringUtils.containsIgnoreCase(identifier.getPartyId(), partyId)).count();
                        return count > 0;
                    };
        }
        return DEFAULT_PREDICATE;
    }

    protected Predicate<Party> processPredicate(final String processName) {
        //Search in the list of process for which this party is initiator and the one for which this party is a responder.
        if (StringUtils.isNotEmpty(processName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("create process predicate for [{}]", processName);
            }
            return
                    party -> {
                        long count = party.getProcessesWithPartyAsInitiator().stream().
                                filter(process -> StringUtils.containsIgnoreCase(process.getName(), processName)).count();
                        count += party.getProcessesWithPartyAsResponder().stream().
                                filter(process -> StringUtils.containsIgnoreCase(process.getName(), processName)).count();
                        return count > 0;
                    };
        }

        return DEFAULT_PREDICATE;
    }

    protected void replaceParties(List<Party> partyList, Configuration configuration) {

        List<eu.domibus.common.model.configuration.Party> list = domainCoreConverter.convert(partyList, eu.domibus.common.model.configuration.Party.class);

        BusinessProcesses bp = configuration.getBusinessProcesses();
        Parties parties = bp.getPartiesXml();
        parties.getParty().clear();
        parties.getParty().addAll(list);

        PartyIdTypes partyIdTypes = bp.getPartiesXml().getPartyIdTypes();
        list.forEach(party -> {
            party.getIdentifiers().forEach(identifier -> {
                if (!partyIdTypes.getPartyIdType().contains(identifier.getPartyIdType())) {
                    partyIdTypes.getPartyIdType().add(identifier.getPartyIdType());
                }
            });
        });

        List<Process> processes = bp.getProcesses();
        processes.forEach(process -> {
            // sync <initiatorParties> and <responderParties>
            Set<String> iParties = partyList.stream()
                    .filter(p -> p.getProcessesWithPartyAsInitiator().stream()
                            .anyMatch(pp -> process.getName().equals(pp.getName())))
                    .map(p -> p.getName())
                    .collect(Collectors.toSet());

            if (process.getInitiatorPartiesXml() == null)
                process.setInitiatorPartiesXml(new InitiatorParties());
            List<InitiatorParty> ip = process.getInitiatorPartiesXml().getInitiatorParty();
            ip.removeIf(x -> !iParties.contains(x.getName()));
            ip.addAll(iParties.stream().filter(name -> ip.stream().noneMatch(x -> name.equals(x.getName())))
                    .map(name -> {
                        InitiatorParty y = new InitiatorParty();
                        y.setName(name);
                        return y;
                    }).collect(Collectors.toSet()));
            if (ip.isEmpty())
                process.setInitiatorPartiesXml(null);


            Set<String> rParties = partyList.stream()
                    .filter(p -> p.getProcessesWithPartyAsResponder().stream()
                            .anyMatch(pp -> process.getName().equals(pp.getName())))
                    .map(p -> p.getName())
                    .collect(Collectors.toSet());

            if (process.getResponderPartiesXml() == null)
                process.setResponderPartiesXml(new ResponderParties());
            List<ResponderParty> rp = process.getResponderPartiesXml().getResponderParty();
            rp.removeIf(x -> !rParties.contains(x.getName()));
            rp.addAll(rParties.stream().filter(name -> rp.stream().noneMatch(x -> name.equals(x.getName())))
                    .map(name -> {
                        ResponderParty y = new ResponderParty();
                        y.setName(name);
                        return y;
                    }).collect(Collectors.toSet()));
            if (rp.isEmpty())
                process.setResponderPartiesXml(null);
        });
    }

    @Override
    public void updateParties(List<Party> partyList, Map<String, String> certificateList) {
        final PModeArchiveInfo pModeArchiveInfo = pModeProvider.getRawConfigurationList().stream().findFirst().orElse(null);
        if (pModeArchiveInfo == null)
            throw new IllegalStateException("Could not update PMode parties: PMode not found!");

        ConfigurationRaw rawConfiguration = pModeProvider.getRawConfiguration(pModeArchiveInfo.getId());

        Configuration configuration;
        try {
            configuration = pModeProvider.getPModeConfiguration(rawConfiguration.getXml());
        } catch (XmlProcessingException e) {
            LOG.error("Error reading current PMode", e);
            throw new IllegalStateException(e);
        }

        replaceParties(partyList, configuration);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ssO");
        ZonedDateTime confDate = ZonedDateTime.ofInstant(rawConfiguration.getConfigurationDate().toInstant(), ZoneId.systemDefault());
        String updatedDescription = "Updated parties to version of " + confDate.format(formatter);

        byte[] updatedPmode;
        try {
            updatedPmode = pModeProvider.serializePModeConfiguration(configuration);
            pModeProvider.updatePModes(updatedPmode, updatedDescription);
        } catch (XmlProcessingException e) {
            LOG.error("Error writing current PMode", e);
            throw new IllegalStateException(e);
        }

        certificateList.entrySet().stream()
                .filter(pair -> pair.getValue() != null)
                .forEach(pair -> {
                    String partyName = pair.getKey();
                    String certificateContent = pair.getValue();
                    X509Certificate cert = certificateService.loadCertificateFromString(certificateContent);
                    multiDomainCertificateProvider.addCertificate(domainProvider.getCurrentDomain(), cert, partyName, true);
        });
    }

    @Override
    public List<eu.domibus.api.process.Process> getAllProcesses() {
        //Retrieve all processes
        List<eu.domibus.common.model.configuration.Process> allProcesses = pModeProvider.findAllProcesses();
        if (LOG.isDebugEnabled()) {
            LOG.debug("findAllProcesses for pmode");
            allProcesses.forEach(process -> LOG.debug("     [{}]", process));
        }

        List<eu.domibus.api.process.Process> processes = domainCoreConverter.convert(allProcesses, eu.domibus.api.process.Process.class);

        if (LOG.isDebugEnabled()) {
            LOG.debug("     party");
            processes.forEach(party -> LOG.debug("[{}]", party));
        }

        return processes;
    }

}
