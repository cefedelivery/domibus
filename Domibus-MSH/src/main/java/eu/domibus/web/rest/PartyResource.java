package eu.domibus.web.rest;

import com.google.common.collect.Lists;
import eu.domibus.api.party.PartyService;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.services.CsvService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.party.IdentifierRo;
import eu.domibus.core.party.PartyResponseRo;
import eu.domibus.core.party.ProcessRo;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RestController
@RequestMapping(value = "/rest/party")
public class PartyResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyResource.class);

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private PartyService partyService;

    @Autowired
    @Qualifier("csvServiceImpl")
    private CsvService csvService;

    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public List<PartyResponseRo> listParties(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "endPoint", required = false) String endPoint,
            @RequestParam(value = "partyId", required = false) String partyId,
            @RequestParam(value = "process", required = false) String process,
            @RequestParam(value = "pageStart", defaultValue = "0") int pageStart,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize
    ) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Searching party with parameters");
            LOG.debug("name [{}]", name);
            LOG.debug("endPoint [{}]", endPoint);
            LOG.debug("partyId [{}]", partyId);
            LOG.debug("processName [{}]", process);
            LOG.debug("pageStart [{}]", pageStart);
            LOG.debug("pageSize [{}]", pageSize);
        }
        List<PartyResponseRo> partyResponseRos = domainConverter.convert(
                partyService.getParties(
                        name,
                        endPoint,
                        partyId,
                        process,
                        pageStart,
                        pageSize),
                PartyResponseRo.class);


        flattenIdentifiers(partyResponseRos);

        flattenProcesses(partyResponseRos);

        return partyResponseRos;
    }

    @RequestMapping(value = {"/count"}, method = RequestMethod.GET)
    public long countParties(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "endPoint", required = false) String endPoint,
            @RequestParam(value = "partyId", required = false) String partyId,
            @RequestParam(value = "process", required = false) String process
    ) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Counting party with parameters");
            LOG.debug("name [{}]", name);
            LOG.debug("endPoint [{}]", endPoint);
            LOG.debug("partyId [{}]", partyId);
            LOG.debug("processName [{}]", process);
        }
        return partyService.countParties(
                name,
                endPoint,
                partyId,
                process
        );
    }

    @RequestMapping(path = "/csv", method = RequestMethod.GET)
    public ResponseEntity<String> getCsv(@RequestParam(value = "name", required = false) String name,
                                         @RequestParam(value = "endPoint", required = false) String endPoint,
                                         @RequestParam(value = "partyId", required = false) String partyId,
                                         @RequestParam(value = "process", required = false) String process) {
        String resultText;
        final List<PartyResponseRo> partyResponseRoList = listParties(name,endPoint,partyId,process,0, 10000);

        List<String> excludedItems = new ArrayList<>();
        excludedItems.add("entityId");
        excludedItems.add("identifiers");
        excludedItems.add("userName");
        excludedItems.add("processesWithPartyAsInitiator");
        excludedItems.add("processesWithPartyAsResponder");
        csvService.setExcludedItems(excludedItems);

        try {
            resultText = csvService.exportToCSV(partyResponseRoList);
        } catch (EbMS3Exception e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/ms-excel"))
                .header("Content-Disposition", "attachment; filename=party_datatable.csv")
                .body(resultText);
    }

    /**
     * Flatten the list of identifiers of each party into a comma separated list for displaying in the console.
     *
     * @param partyResponseRos the list of party to be adapted.
     */
    protected void flattenIdentifiers(List<PartyResponseRo> partyResponseRos) {
        partyResponseRos.forEach(
                partyResponseRo -> {
                    String joinedIdentifiers = partyResponseRo.getIdentifiers().
                            stream().
                            map(IdentifierRo::getPartyId).
                            sorted().
                            collect(Collectors.joining(", "));
                    partyResponseRo.setJoinedIdentifiers(joinedIdentifiers);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Flatten identifiers for [{}]=[{}]", partyResponseRo.getName(), partyResponseRo.getJoinedIdentifiers());
                    }
                });
    }


    /**
     * Flatten the list of processes of each party into a comma separated list for displaying in the console.
     *
     * @param partyResponseRos the list of party to be adapted.
     */
    protected void flattenProcesses(List<PartyResponseRo> partyResponseRos) {
        partyResponseRos.forEach(
                partyResponseRo -> {

                    List<ProcessRo> processesWithPartyAsInitiator = partyResponseRo.getProcessesWithPartyAsInitiator();
                    List<ProcessRo> processesWithPartyAsResponder = partyResponseRo.getProcessesWithPartyAsResponder();

                    List<ProcessRo> processesWithPartyAsInitiatorAndResponder =
                            processesWithPartyAsInitiator.
                                    stream().
                                    filter(processesWithPartyAsResponder::contains).
                                    collect(Collectors.toList());

                    List<ProcessRo> processWithPartyAsInitiatorOnly = processesWithPartyAsInitiator
                            .stream()
                            .filter(processRo -> !processesWithPartyAsInitiatorAndResponder.contains(processRo))
                            .collect(Collectors.toList());

                    List<ProcessRo> processWithPartyAsResponderOnly = processesWithPartyAsResponder
                            .stream()
                            .filter(processRo -> !processesWithPartyAsInitiatorAndResponder.contains(processRo))
                            .collect(Collectors.toList());

                    String joinedProcessesWithMeAsInitiatorOnly = processWithPartyAsInitiatorOnly.
                            stream().
                            map(ProcessRo::getName).
                            map(name -> name.concat("(I)")).
                            collect(Collectors.joining(", "));

                    String joinedProcessesWithMeAsResponderOnly = processWithPartyAsResponderOnly.
                            stream().
                            map(ProcessRo::getName).
                            map(name -> name.concat("(R)")).
                            collect(Collectors.joining(","));

                    String joinedProcessesWithMeAsInitiatorAndResponder = processesWithPartyAsInitiatorAndResponder.
                            stream().
                            map(ProcessRo::getName).
                            map(name -> name.concat("(IR)")).
                            collect(Collectors.joining(","));

                    List<String> joinedProcess= Lists.newArrayList();

                    if(StringUtils.isNotEmpty(joinedProcessesWithMeAsInitiatorOnly)){
                        joinedProcess.add(joinedProcessesWithMeAsInitiatorOnly);
                    }

                    if(StringUtils.isNotEmpty(joinedProcessesWithMeAsResponderOnly)){
                        joinedProcess.add(joinedProcessesWithMeAsResponderOnly);
                    }

                    if(StringUtils.isNotEmpty(joinedProcessesWithMeAsInitiatorAndResponder)){
                        joinedProcess.add(joinedProcessesWithMeAsInitiatorAndResponder);
                    }

                    partyResponseRo.setJoinedProcesses(
                            StringUtils.join(joinedProcess,", "));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Flatten processes for [{}]=[{}]", partyResponseRo.getName(), partyResponseRo.getJoinedProcesses());
                    }
                });
    }
}
