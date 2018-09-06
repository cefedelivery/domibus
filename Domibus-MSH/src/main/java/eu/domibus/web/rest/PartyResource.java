package eu.domibus.web.rest;

import com.google.common.collect.Lists;
import eu.domibus.api.csv.CsvException;
import eu.domibus.api.party.Party;
import eu.domibus.api.party.PartyService;
import eu.domibus.api.security.TrustStoreEntry;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.csv.CsvCustomColumns;
import eu.domibus.core.csv.CsvExcludedItems;
import eu.domibus.core.csv.CsvService;
import eu.domibus.core.csv.CsvServiceImpl;
import eu.domibus.core.party.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.pki.CertificateService;
import eu.domibus.web.rest.ro.TrustStoreRO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RestController
@RequestMapping(value = "/rest/party")
public class PartyResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartyResource.class);
    private static final String DELIMITER = ", ";

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private PartyService partyService;

    @Autowired
    private CsvServiceImpl csvServiceImpl;

    @Autowired
    private CertificateService certificateService;

    @GetMapping(value = {"/list"})
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

        if (pageStart <= 0) {
            pageStart = 0;
        }
        if (pageSize <= 0) {
            pageSize = Integer.MAX_VALUE;
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

        partyResponseRos.forEach(partyResponseRo -> {
            final List<ProcessRo> processesWithPartyAsInitiator = partyResponseRo.getProcessesWithPartyAsInitiator();
            final List<ProcessRo> processesWithPartyAsResponder = partyResponseRo.getProcessesWithPartyAsResponder();

            final Set<ProcessRo> processRos = new HashSet<>(processesWithPartyAsInitiator);
            processRos.addAll(processesWithPartyAsResponder);

            processRos
                    .stream()
                    .map(item -> new PartyProcessLinkRo(item.getName(), processesWithPartyAsInitiator.contains(item), processesWithPartyAsResponder.contains(item)))
                    .collect(Collectors.toSet());
        });

        return partyResponseRos;
    }

    /**
     * This method returns a CSV file with the contents of Party table
     *
     * @param name     the party name
     * @param endPoint the party endpoint
     * @param partyId  the party id
     * @param process  a process associated with the party
     * @return CSV file with the contents of Party table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv(@RequestParam(value = "name", required = false) String name,
                                         @RequestParam(value = "endPoint", required = false) String endPoint,
                                         @RequestParam(value = "partyId", required = false) String partyId,
                                         @RequestParam(value = "process", required = false) String process) {
        String resultText;
        final List<PartyResponseRo> partyResponseRoList = listParties(name, endPoint, partyId, process, 0, csvServiceImpl.getMaxNumberRowsToExport());

        try {
            resultText = csvServiceImpl.exportToCSV(partyResponseRoList, PartyResponseRo.class,
                    CsvCustomColumns.PARTY_RESOURCE.getCustomColumns(), CsvExcludedItems.PARTY_RESOURCE.getExcludedItems());
        } catch (CsvException e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CsvService.APPLICATION_EXCEL_STR))
                .header("Content-Disposition", "attachment; filename=" + csvServiceImpl.getCsvFilename("pmodeparties"))
                .body(resultText);
    }

    @RequestMapping(value = {"/update"}, method = RequestMethod.PUT)
    public ResponseEntity updateParties(@RequestBody List<PartyResponseRo> partiesRo) {
        LOG.debug("Updating parties [{}]", Arrays.toString(partiesRo.toArray()));

        List<Party> partyList = domainConverter.convert(partiesRo, Party.class);
        LOG.debug("Updating partyList [{}]", partyList.toArray());

        Map<String, String> certificates = partiesRo.stream()
                .filter(party -> party.getCertificateContent() != null)
                .collect(Collectors.toMap(PartyResponseRo::getName, PartyResponseRo::getCertificateContent));

        try {
            partyService.updateParties(partyList, certificates);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            StringBuilder errorMessageB = new StringBuilder();
            for (Throwable err = e; err != null; err = err.getCause()) {
                errorMessageB.append("\n").append(err.getMessage());
            }
            return ResponseEntity.badRequest().body(errorMessageB.toString());
        }
    }

    /**
     * Flatten the list of identifiers of each party into a comma separated list
     * for displaying in the console.
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
                            collect(Collectors.joining(DELIMITER));
                    partyResponseRo.setJoinedIdentifiers(joinedIdentifiers);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Flatten identifiers for [{}]=[{}]", partyResponseRo.getName(), partyResponseRo.getJoinedIdentifiers());
                    }
                });
    }

    /**
     * Flatten the list of processes of each party into a comma separated list
     * for displaying in the console.
     *
     * @param partyResponseRos the list of party to be adapted.
     */
    protected void flattenProcesses(List<PartyResponseRo> partyResponseRos) {
        partyResponseRos.forEach(
                partyResponseRo -> {

                    List<ProcessRo> processesWithPartyAsInitiator = partyResponseRo.getProcessesWithPartyAsInitiator();
                    List<ProcessRo> processesWithPartyAsResponder = partyResponseRo.getProcessesWithPartyAsResponder();

                    List<ProcessRo> processesWithPartyAsInitiatorAndResponder
                            = processesWithPartyAsInitiator.
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
                            collect(Collectors.joining(DELIMITER));

                    String joinedProcessesWithMeAsResponderOnly = processWithPartyAsResponderOnly.
                            stream().
                            map(ProcessRo::getName).
                            map(name -> name.concat("(R)")).
                            collect(Collectors.joining(DELIMITER));

                    String joinedProcessesWithMeAsInitiatorAndResponder = processesWithPartyAsInitiatorAndResponder.
                            stream().
                            map(ProcessRo::getName).
                            map(name -> name.concat("(IR)")).
                            collect(Collectors.joining(DELIMITER));

                    List<String> joinedProcess = Lists.newArrayList();

                    if (StringUtils.isNotEmpty(joinedProcessesWithMeAsInitiatorOnly)) {
                        joinedProcess.add(joinedProcessesWithMeAsInitiatorOnly);
                    }

                    if (StringUtils.isNotEmpty(joinedProcessesWithMeAsResponderOnly)) {
                        joinedProcess.add(joinedProcessesWithMeAsResponderOnly);
                    }

                    if (StringUtils.isNotEmpty(joinedProcessesWithMeAsInitiatorAndResponder)) {
                        joinedProcess.add(joinedProcessesWithMeAsInitiatorAndResponder);
                    }

                    partyResponseRo.setJoinedProcesses(
                            StringUtils.join(joinedProcess, DELIMITER));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Flatten processes for [{}]=[{}]", partyResponseRo.getName(), partyResponseRo.getJoinedProcesses());
                    }
                });
    }

    @GetMapping(value = {"/processes"})
    public List<ProcessRo> listProcesses() {
        return domainConverter.convert(partyService.getAllProcesses(), ProcessRo.class);
    }

    @GetMapping(value = "/{partyName}/certificate")
    public ResponseEntity<TrustStoreRO> getCertificateForParty(@PathVariable(name = "partyName") String partyName) {
        try {
            TrustStoreEntry cert = certificateService.getPartyCertificateFromTruststore(partyName);
            if (cert == null) {
                return ResponseEntity.notFound().build();
            }
            TrustStoreRO res = domainConverter.convert(cert, TrustStoreRO.class);
            return ResponseEntity.ok(res);
        } catch (KeyStoreException e) {
            LOG.error("Failed to get certificate from truststore", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(value = "/{partyName}/certificate")
    public TrustStoreRO convertCertificateContent(@PathVariable(name = "partyName") String partyName,
                                                  @RequestBody CertificateContentRo certificate) {

        if (certificate == null) {
            throw new IllegalArgumentException("certificate parameter must be provided");
        }

        String content = certificate.getContent();
        LOG.debug("certificate base 64 received [{}] ", content);

        TrustStoreEntry cert = null;
        try {
            cert = certificateService.convertCertificateContent(content);
        } catch (CertificateException e) {
            throw new IllegalArgumentException("certificate could not be parsed");
        }
        if (cert == null) {
            throw new IllegalArgumentException("certificate could not be parsed");
        }

        return domainConverter.convert(cert, TrustStoreRO.class);
    }
}
