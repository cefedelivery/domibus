package eu.domibus.core.party;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.domibus.api.party.Party;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class PartyServiceImpl implements PartyService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyServiceImpl.class);


    @Autowired
    private DomainCoreConverter domainCoreConverter;

    @Autowired
    private PModeProvider pModeProvider;

    private final static Predicate<Party> DEFAULT_PREDICATE = condition -> true;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Party> listParties(final String name,
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
     *{@inheritDoc}
     */
    @Override
    public long countParties(String name, String endPoint, String partyId, String processName) {
        final Predicate<Party> searchPredicate = getSearchPredicate(name, endPoint, partyId, processName);
        return linkPartyAndProcesses().
                stream().
                filter(searchPredicate).
                count();
    }

    private Predicate<Party> getSearchPredicate(String name, String endPoint, String partyId, String processName) {
        return namePredicate(name).
                and(endPointPredicate(endPoint)).
                and(partyIdPredicate(partyId)).
                and(processPredicate(processName));
    }



    private Predicate<Party> namePredicate(final String name) {

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

    private Predicate<Party> endPointPredicate(final String endPoint) {
        if (StringUtils.isNotEmpty(endPoint)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("create endPoint predicate for [{}]", endPoint);
            }
            return party ->
                    StringUtils.containsIgnoreCase(party.getEndpoint(), endPoint.toUpperCase());
        }
        return DEFAULT_PREDICATE;
    }

    private Predicate<Party> partyIdPredicate(final String partyId) {
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

    private Predicate<Party> processPredicate(final String processName) {
        //Search in the list of process for which this party is initiator and the one for which this party is a responder.
        if (StringUtils.isNotEmpty(processName)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("create process predicate for [{}]", processName);
            }
            return
                    party -> {
                        long count = party.getProcessesWithMeAsInitiator().stream().
                                filter(process -> StringUtils.containsIgnoreCase(process.getName(), processName)).count();
                        count += party.getProcessesWithMeAsResponder().stream().
                                filter(process -> StringUtils.containsIgnoreCase(process.getName(), processName)).count();
                        return count > 0;
                    };
        }

        return DEFAULT_PREDICATE;
    }

    /**
     * In the actual configuration the link between parties and processes exists from process to party.
     * We need to reverse this association, we want to have a relation party -> process I am involved in as a responder
     * or initiator.
     *
     * @return a list of party linked with their processes.
     */
    private List<Party> linkPartyAndProcesses() {

        //Retrieve all party entities.
        List<eu.domibus.common.model.configuration.Party> allParties = pModeProvider.findAllParties();
        if (LOG.isDebugEnabled()) {
            LOG.debug("linkPartyAndProcesses for party entities");
            allParties.forEach(party -> LOG.debug("     [{}]", party));
        }

        //create a new Party to live outside the service per existing party entity in the pmode.
        List<Party> parties =
                allParties.
                        stream().map(party -> domainCoreConverter.convert(party, Party.class)).
                        collect(Collectors.toList());

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

        linkProcessWithMeAsInitiator(partyMapByName, allProcesses);

        linProcessWithMeAsResponder(partyMapByName, allProcesses);

        if (LOG.isDebugEnabled()) {
            LOG.debug("     party");
            parties.forEach(party -> LOG.debug("[{}]", party));
        }

        return parties;
    }


    private void linkProcessWithMeAsInitiator(final Map<String, Party> partyMapByName, final List<Process> allProcesses) {
        allProcesses.forEach(
                processEntity -> {
                    //loop process initiators.
                    processEntity.getInitiatorParties().forEach(partyEntity -> {
                                Party party = partyMapByName.get(partyEntity.getName());
                                eu.domibus.api.party.Process process = domainCoreConverter.convert(
                                        processEntity,
                                        eu.domibus.api.party.Process.class);
                                //add the processes for which this party is initiator.
                                party.addProcessesWithMeAsInitiator(process);
                            }
                    );
                }
        );
    }

    private void linProcessWithMeAsResponder(final Map<String, Party> partyMapByName, final List<Process> allProcesses) {
        allProcesses.forEach(
                processEntity -> {
                    //loop process responder.
                    processEntity.getResponderParties().forEach(partyEntity -> {
                                Party party = partyMapByName.get(partyEntity.getName());
                                eu.domibus.api.party.Process process = domainCoreConverter.convert(
                                        processEntity,
                                        eu.domibus.api.party.Process.class);
                                //add the processes for which this party is responder.
                                party.addProcessesWithMeAsResponder(process);
                            }
                    );
                }
        );
    }
}
