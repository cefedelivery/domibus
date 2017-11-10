package eu.domibus.web.rest;

import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.party.IdentifierRo;
import eu.domibus.core.party.PartyResponseRo;
import eu.domibus.core.party.PartyService;
import eu.domibus.core.party.ProcessRo;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                partyService.listParties(
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

    private void flattenIdentifiers(List<PartyResponseRo> partyResponseRos) {
        partyResponseRos.forEach(
                partyResponseRo -> {
                    String joinedIdentifiers = partyResponseRo.getIdentifiers().
                            stream().
                            map(IdentifierRo::getPartyId).
                            collect(Collectors.joining(","));
                    partyResponseRo.setJoinedIdentifiers(joinedIdentifiers);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Flatten identifiers for [{}]=[{}]", partyResponseRo.getName(), partyResponseRo.getJoinedIdentifiers());
                    }
                });
    }

    private void flattenProcesses(List<PartyResponseRo> partyResponseRos) {
        partyResponseRos.forEach(
                partyResponseRo -> {
                    String joinedProcessesWithMeAsInitiator = partyResponseRo.getProcessesWithMeAsInitiator().
                            stream().
                            map(ProcessRo::getName).
                            collect(Collectors.joining(","));

                    String joinedProcessesWithMeAsResponder = partyResponseRo.getProcessesWithMeAsResponder().
                            stream().
                            map(ProcessRo::getName).
                            collect(Collectors.joining(","));
                    partyResponseRo.setJoinedProcesses(joinedProcessesWithMeAsInitiator + "     " + joinedProcessesWithMeAsResponder);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Flatten processes for [{}]=[{}]", partyResponseRo.getName(), partyResponseRo.getJoinedProcesses());
                    }
                });
    }
}
