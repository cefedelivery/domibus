package eu.domibus.core.party;

import com.google.common.collect.Lists;
import eu.domibus.common.dao.PartyDao;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.ebms3.common.dao.PModeProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@Service
public class PartyServiceImpl implements PartyService {

    @Autowired
    private PartyDao partyDao;

    @Autowired
    private DomainCoreConverter domainCoreConverter;

    @Autowired
    private PModeProvider pModeProvider;

    /**
     * @param name
     * @param endPoint
     * @param partyId
     * @param processName
     * @param pargeStart
     * @param pageSize
     * @return
     */
    @Override
    public List<Party> listParties(final String name,
                                   final String endPoint,
                                   final String partyId,
                                   final String processName,
                                   int pargeStart,
                                   int pageSize) {

        Predicate<Party> searchPredicate = preparePredicate(
                name,
                endPoint,
                partyId,
                processName);

        return linkPartyAndProcesses().stream().
                filter(searchPredicate).
                collect(Collectors.toList());

    }

    /**
     * Create a predicate that will be used to filter the object hierarchy we have in memory.
     *
     * @param name        the name of the party.
     * @param endPoint    the endpoint of the party.
     * @param partyId     a partyid name.
     * @param processName the name of a process the party is involved in.
     * @return a predicate.
     */
    private Predicate<Party> preparePredicate(String name, String endPoint, String partyId, String processName) {

        Predicate<Party> defaultPredicate = processPartyAssociation -> true;
        if (StringUtils.isNotEmpty(name)) {
            defaultPredicate.and(
                    party ->
                            StringUtils.containsIgnoreCase(party.getName(), name.toUpperCase())
            );
        }

        if (StringUtils.isNotEmpty(endPoint)) {
            defaultPredicate.and(
                    party ->
                            StringUtils.containsIgnoreCase(party.getEndpoint(), endPoint.toUpperCase()));
        }
        //Search in the list of partyId to find one that match the search criteria.
        if (StringUtils.isNotEmpty(partyId)) {
            defaultPredicate.and(
                    party -> {
                        long count = party.getIdentifiers().stream().
                                filter(identifier -> StringUtils.containsIgnoreCase(identifier.getPartyId(), partyId)).count();
                        return count > 0;
                    });
        }

        //Search in the list of process for which this party is initiator and the one for which this party is a responder.
        if (StringUtils.isNotEmpty(processName)) {
            defaultPredicate.and(
                    party -> {
                        long count = party.getProcessesWithMeAsInitiator().stream().
                                filter(process -> StringUtils.containsIgnoreCase(process.getName(), processName)).count();
                        count += party.getProcessesWithMeAsResponder().stream().
                                filter(process -> StringUtils.containsIgnoreCase(process.getName(), processName)).count();
                        return count > 0;
                    });
        }

        return defaultPredicate;
    }

    /**
     * In the actual configuration the link between parties and processes exists from process to party.
     * We need to reverse this association, we want to have a relation party -> process I am involved in as a responder
     * or initiation.
     *
     * @return
     */
    private List<Party> linkPartyAndProcesses() {

        //Retrieve all party entities.
        List<eu.domibus.common.model.configuration.Party> allParties = pModeProvider.findAllParties();

        //create a new Party to live outside the service per existing party entity in the pmode.
        List<Party> parties =
                allParties.
                        stream().map(party -> domainCoreConverter.convert(party, Party.class)).
                        collect(Collectors.toList());

        //transform parties to map for convenience.
        Map<String, Party> partyMapByName = parties.stream().collect(Collectors.toMap(Party::getName, Function.identity()));

        //retrieve all existing processes in the pmode.
        List<Process> allProcesses = pModeProvider.findAllProcesses();

        //loop processes.
        allProcesses.forEach(
                processEntity -> {
                    //loop process initiators.
                    processEntity.getInitiatorParties().forEach(partyEntity -> {
                                Party party = partyMapByName.get(partyEntity.getName());
                                eu.domibus.core.party.Process process = domainCoreConverter.convert(
                                        processEntity,
                                        eu.domibus.core.party.Process.class);
                                //add the processes for which this party is initiator.
                                party.addProcessesWithMeAsInitiator(process);
                            }
                    );

                    //loop process responder.
                    processEntity.getInitiatorParties().forEach(partyEntity -> {
                                Party party = partyMapByName.get(partyEntity.getName());
                                eu.domibus.core.party.Process process = domainCoreConverter.convert(
                                        processEntity,
                                        eu.domibus.core.party.Process.class);
                                //add the processes for which this party is responder.
                                party.addProcessesWithMeAsInitiator(process);
                            }
                    );
                }
        );
        return parties;

    }


}
