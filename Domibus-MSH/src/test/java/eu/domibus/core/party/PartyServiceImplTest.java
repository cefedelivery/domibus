package eu.domibus.core.party;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.party.Identifier;
import eu.domibus.api.party.Party;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.process.Process;
import eu.domibus.common.dao.PartyDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.CertificateEntry;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.ebms3.common.model.Ebms3Constants;
import eu.domibus.messaging.XmlProcessingException;
import eu.domibus.pki.CertificateService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.Verifications;
import mockit.VerificationsInOrder;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@RunWith(JMockit.class)
public class PartyServiceImplTest {

    @Injectable
    private DomainCoreConverter domainCoreConverter;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private PartyDao partyDao;

    @Injectable
    private MultiDomainCryptoService multiDomainCertificateProvider;

    @Injectable
    private DomainContextProvider domainProvider;

    @Injectable
    private CertificateService certificateService;

    @Tested
    private PartyServiceImpl partyService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Injectable
    private eu.domibus.common.model.configuration.Party gatewayParty;

    @Injectable
    private Configuration configuration;

    @Injectable
    private BusinessProcesses configurationBusinessProcesses;

    @Injectable
    private Parties configurationParties;

    @Injectable
    private PartyIdTypes configurationPartyIdTypes;

    @Injectable
    private Domain currentDomain;

    @Before
    public void setUp() {
        new NonStrictExpectations() {{
            gatewayParty.getName(); result = "gatewayParty";
            pModeProvider.getGatewayParty(); result = gatewayParty;

            configuration.getBusinessProcesses(); result = configurationBusinessProcesses;
            configurationBusinessProcesses.getPartiesXml(); result = configurationParties;
            configurationParties.getPartyIdTypes(); result = configurationPartyIdTypes;

            domainProvider.getCurrentDomain(); result = currentDomain;
        }};
    }

    @Test
    public void getParties() {
        String name = "name";
        String endPoint = "endPoint";
        String partyId = "partyId";
        String processName = "processName";
        int pageStart=0;
        int pageSize=10;

        new Expectations(partyService){{
            partyService.getSearchPredicate(anyString,anyString,anyString,anyString);
            partyService.linkPartyAndProcesses();times=1;
        }};
        partyService.getParties(name, endPoint, partyId, processName,pageStart,pageSize);
        new Verifications(){{
            partyService.getSearchPredicate(name,endPoint,partyId,processName);times=1;
        }};
    }

    @Test
    public void linkPartyAndProcesses() {
        eu.domibus.common.model.configuration.Party partyEntity=new eu.domibus.common.model.configuration.Party();
        final String name = "name";
        partyEntity.setName(name);

        List<eu.domibus.common.model.configuration.Party> partyEntities = Lists.newArrayList(partyEntity);
        List<eu.domibus.common.model.configuration.Process> processEntities = Lists.newArrayList(new eu.domibus.common.model.configuration.Process());

        Party party=new Party();
        party.setName(name);
        List<Party> parties = Lists.newArrayList(party);

        new Expectations(partyService){{
            pModeProvider.findAllParties();
            result=partyEntities;
            pModeProvider.findAllProcesses();
            result=processEntities;
            domainCoreConverter.convert(partyEntities,Party.class);
            result=parties;
            partyService.linkProcessWithPartyAsInitiator(withAny(new HashMap<>()),processEntities);times=1;
            partyService.linkProcessWithPartyAsResponder(withAny(new HashMap<>()),processEntities);times=1;
        }};

        partyService.linkPartyAndProcesses();

        new Verifications(){{
            Map<String,Party> partyMap;
            partyService.linkProcessWithPartyAsInitiator(partyMap=withCapture(),processEntities);times=1;
            assertEquals(partyMap.get(name),party);
            partyService.linkProcessWithPartyAsResponder(partyMap=withCapture(),processEntities);times=1;
            assertEquals(partyMap.get(name),party);
        }};
    }

    @Test
    public void returnsEmptyListWhenLinkingProcessWithParty_findAllPartiesThrowsIllegalStateException() {
        new Expectations(partyService) {{
            pModeProvider.findAllParties();
            result = new IllegalStateException();
        }};

        List<Party> parties = partyService.linkPartyAndProcesses();

        assertTrue("The party list should have been empty", parties.isEmpty());
    }

    @Test
    public void linkProcessWithPartyAsInitiator(final @Mocked eu.domibus.common.model.configuration.Process processEntity) {
        Party party=new Party();
        party.setName("name");
        Map<String,Party> partyMap=new HashMap<>();
        partyMap.put("name",party);

        Process process = new Process();
        process.setName("p1");


        eu.domibus.common.model.configuration.Party partyEntity=new eu.domibus.common.model.configuration.Party();
        partyEntity.setName("name");

        Set<eu.domibus.common.model.configuration.Party> responderParties = Sets.newHashSet(partyEntity);
        List<eu.domibus.common.model.configuration.Process> processes = Lists.newArrayList(processEntity);

        new Expectations(){{
            processEntity.getInitiatorParties();
            result=responderParties;

            domainCoreConverter.convert(processEntity,Process.class);
            result=process;
        }};
        partyService.linkProcessWithPartyAsInitiator(partyMap,processes);
        assertEquals(1,party.getProcessesWithPartyAsInitiator().size());
        assertEquals("p1",party.getProcessesWithPartyAsInitiator().get(0).getName());
    }

    @Test
    public void linkProcessWithPartyAsResponder(final @Mocked eu.domibus.common.model.configuration.Process processEntity) {
        Party party=new Party();
        party.setName("name");
        Map<String,Party> partyMap=new HashMap<>();
        partyMap.put("name",party);

        Process process = new Process();
        process.setName("p1");


        eu.domibus.common.model.configuration.Party partyEntity=new eu.domibus.common.model.configuration.Party();
        partyEntity.setName("name");

        Set<eu.domibus.common.model.configuration.Party> responderParties = Sets.newHashSet(partyEntity);
        List<eu.domibus.common.model.configuration.Process> processes = Lists.newArrayList(processEntity);

        new Expectations(){{
           processEntity.getResponderParties();
           result=responderParties;

           domainCoreConverter.convert(processEntity,Process.class);
           result=process;
        }};
        partyService.linkProcessWithPartyAsResponder(partyMap,processes);
        assertEquals(1,party.getProcessesWithPartyAsResponder().size());
        assertEquals("p1",party.getProcessesWithPartyAsResponder().get(0).getName());

    }

    @Test
    public void getSearchPredicate() throws Exception {
        final String name = "name";
        final String endPoint = "endPoint";
        final String partyId = "partyId";
        final String processName = "processName";

        new Expectations(partyService){{

        }};
        partyService.getSearchPredicate(name, endPoint, partyId, processName);

        new Verifications(){{
            partyService.namePredicate(name);times=1;
            partyService.endPointPredicate(endPoint);times=1;
            partyService.partyIdPredicate(partyId);times=1;
            partyService.processPredicate(processName);times=1;
        }};
    }

    @Test
    public void namePredicate() throws Exception {
        Party party=new Party();
        party.setName("name");

        assertTrue(partyService.namePredicate("").test(party));
        assertTrue(partyService.namePredicate("name").test(party));
        assertFalse(partyService.namePredicate("wrong").test(party));
    }

    @Test
    public void endPointPredicate() throws Exception {
        Party party=new Party();
        party.setEndpoint("http://localhost:8080");
        assertTrue(partyService.endPointPredicate("").test(party));
        assertTrue(partyService.endPointPredicate("8080").test(party));
        assertFalse(partyService.endPointPredicate("7070").test(party));
    }

    @Test
    public void partyIdPredicate() throws Exception {
        Party party=new Party();
        Identifier identifier = new Identifier();
        identifier.setPartyId("partyId");
        party.setIdentifiers(Sets.newHashSet(identifier));
        party.setIdentifiers(Sets.newHashSet(identifier));
        assertTrue(partyService.partyIdPredicate("").test(party));
        assertTrue(partyService.partyIdPredicate("party").test(party));
        assertFalse(partyService.partyIdPredicate("wrong").test(party));
    }

    @Test
    public void processPredicate() throws Exception {
        Party party=new Party();
        Process process = new Process();
        process.setName("processName");
        party.addProcessesWithPartyAsInitiator(process);
        party.addprocessesWithPartyAsResponder(process);
        assertTrue(partyService.processPredicate(null).test(party));
        assertTrue(partyService.processPredicate("cessName").test(party));
        assertFalse(partyService.processPredicate("wrong").test(party));
    }

    @Test
    public void testFindPartyNamesByServiceAndAction() throws EbMS3Exception {
        // Given
        List<String> parties = new ArrayList<>();
        parties.add("test");
        new Expectations() {{
           pModeProvider.findPartyIdByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);
           result = parties;
        }};

        // When
        List<String> partyNamesByServiceAndAction = partyService.findPartyNamesByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);

        // Then
        Assert.assertEquals(parties, partyNamesByServiceAndAction);
    }

    @Test
    public void testGetGatewayPartyIdentifier() {
        // Given
        String expectedGatewayPartyId = "testGatewayPartyId";
        eu.domibus.common.model.configuration.Party gatewayParty = new eu.domibus.common.model.configuration.Party();
        Set<eu.domibus.common.model.configuration.Identifier> identifiers = new HashSet<>();
        eu.domibus.common.model.configuration.Identifier identifier = new eu.domibus.common.model.configuration.Identifier();
        identifier.setPartyId(expectedGatewayPartyId);
        identifiers.add(identifier);
        gatewayParty.setIdentifiers(identifiers);
        new Expectations() {{
            pModeProvider.getGatewayParty();
            result = gatewayParty;
        }};

        // When
        String gatewayPartyId = partyService.getGatewayPartyIdentifier();

        // Then
        Assert.assertEquals(expectedGatewayPartyId, gatewayPartyId);
    }

    @Test
    public void getProcesses() {
        new Expectations() {{
            pModeProvider.findAllProcesses();
        }};

        // When
        partyService.getAllProcesses();
    }

    @Test
    public void returnsEmptyListWhenRetrievingAllProcesses_findAllProcessesThrowsIllegalStateException() {
        // Given
        new Expectations(partyService) {{
            pModeProvider.findAllProcesses();
            result = new IllegalStateException();
        }};

        // When
        List<Process> processes = partyService.getAllProcesses();

        // Then
        assertTrue("The process list should have been empty", processes.isEmpty());
    }

    @Test
    public void failsWhenReplacingPartiesIfTheNewReplacementPartiesDoNotContainTheGatewayPartyDefinitionAsCurrentlyPresentInConfiguration(
            @Injectable eu.domibus.common.model.configuration.Party replacement) {
        // Expected exception
        thrown.expect(DomibusCoreException.class);
        thrown.expectMessage("Cannot delete the party describing the current system. ");

        // Given
        List<Party> replacements = Lists.newArrayList();
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(replacement);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);

        new Expectations() {{
            replacement.getName(); result = "replacementParty"; // update the replacement party

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
        }};

        // When
        partyService.replaceParties(replacements, configuration);
    }

    @Test
    public void addsPartyIdentifierTypesToTheOnesCurrentlyPresentInConfigurationWhenReplacingParties(@Injectable eu.domibus.common.model.configuration.Party converted,
             @Injectable PartyIdType partyIdType,
             @Injectable PartyIdType matchingConfigurationPartyIdType,
             @Injectable PartyIdType nonMatchingConfigurationPartyIdType,
             @Injectable eu.domibus.common.model.configuration.Identifier firstParty,
             @Injectable eu.domibus.common.model.configuration.Identifier secondParty) {
        // Given
        List<Party> replacements = Lists.newArrayList(); // ignore content, just use an empty list
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(converted);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);
        List<PartyIdType> partyIdTypes = Lists.newArrayList(partyIdType);

        new Expectations() {{
            partyIdType.equals(matchingConfigurationPartyIdType); result = true; // invoked by List#contains
            partyIdType.equals(nonMatchingConfigurationPartyIdType); result = false; // invoked by List#contains

            firstParty.getPartyIdType(); result = matchingConfigurationPartyIdType;
            secondParty.getPartyIdType(); result = nonMatchingConfigurationPartyIdType;

            converted.getName(); result = "gatewayParty"; // update the gateway party
            converted.getIdentifiers(); result = Sets.newHashSet(firstParty, secondParty);

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
            configurationPartyIdTypes.getPartyIdType(); result = partyIdTypes;
        }};

        // When
        partyService.replaceParties(replacements, configuration);

        // Then
        new VerificationsInOrder() {{
            PartyIdType expected = new PartyIdType();

            expected.setName("id_2");
            partyIdTypes.add(withEqual(expected));

            expected.setName("id_3");
            partyIdTypes.add(withEqual(expected));
        }};
    }

    @Test
    public void removesInitiatorPartiesFromProcessConfigurationIfThePartiesBeingReplacedDoNotBelongToThoseProcessesAnymoreWhenReplacingParties(@Injectable Party replacement,
             @Injectable Process process,
             @Injectable eu.domibus.common.model.configuration.Process configurationProcess,
             @Injectable InitiatorParties configurationInitiatorParties,
             @Injectable InitiatorParty configurationInitiatorParty,
             @Injectable eu.domibus.common.model.configuration.Party converted) {
        // Given
        List<Party> replacements = Lists.newArrayList(replacement);
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(converted);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);
        List<InitiatorParty> configurationInitiatorPartyList = Lists.newArrayList(configurationInitiatorParty);

        new Expectations() {{
            process.getName(); result = "process_1";
            configurationProcess.getName(); result = "process_1";
            replacement.getName(); result = "gatewayParty";
            converted.getName(); result = "gatewayParty"; // update the gateway party
            configurationInitiatorParty.getName(); result = "configurationParty";

            configurationProcess.getInitiatorPartiesXml(); result = configurationInitiatorParties;
            configurationInitiatorParties.getInitiatorParty(); result = configurationInitiatorPartyList;

            replacement.getProcessesWithPartyAsInitiator(); result = Lists.newArrayList(process);

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
            configurationBusinessProcesses.getProcesses(); result = Lists.newArrayList(configurationProcess);
        }};

        // When
        partyService.replaceParties(replacements, configuration);

        // Then
        new Verifications() {{
            configurationInitiatorPartyList.remove(configurationInitiatorParty);
        }};
    }

    @Test
    public void doesNotAddInitiatorPartiesIfAlreadyExistingInsideProcessConfigurationWhenReplacingParties(@Injectable Party replacement,
           @Injectable Process process,
           @Injectable eu.domibus.common.model.configuration.Process configurationProcess,
           @Injectable InitiatorParties configurationInitiatorParties,
           @Injectable InitiatorParty configurationInitiatorParty,
           @Injectable eu.domibus.common.model.configuration.Party converted) {
        // Given
        List<Party> replacements = Lists.newArrayList(replacement);
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(converted);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);
        List<InitiatorParty> configurationInitiatorPartyList = Lists.newArrayList(configurationInitiatorParty);

        new Expectations() {{
            process.getName(); result = "process_1";
            configurationProcess.getName(); result = "process_1";
            replacement.getName(); result = "gatewayParty";
            converted.getName(); result = "gatewayParty"; // update the gateway party
            configurationInitiatorParty.getName(); result = "gatewayParty";

            configurationProcess.getInitiatorPartiesXml(); result = configurationInitiatorParties;
            configurationInitiatorParties.getInitiatorParty(); result = configurationInitiatorPartyList;

            replacement.getProcessesWithPartyAsInitiator(); result = Lists.newArrayList(process);

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
            configurationBusinessProcesses.getProcesses(); result = Lists.newArrayList(configurationProcess);
        }};

        // When
        partyService.replaceParties(replacements, configuration);

        // Then
        Assert.assertEquals("Should have not added the initiator party to the configuration process if already present",
                1, configurationInitiatorPartyList.size());
    }


    @Test
    public void addsInitiatorPartiesIfMissingInsideProcessConfigurationWhenReplacingParties(@Injectable Party gatewayReplacement,
            @Injectable Party replacement,
            @Injectable Process process,
            @Injectable eu.domibus.common.model.configuration.Process configurationProcess,
            @Injectable InitiatorParties configurationInitiatorParties,
            @Injectable InitiatorParty configurationInitiatorParty,
            @Injectable eu.domibus.common.model.configuration.Party converted) {
        // Given
        List<Party> replacements = Lists.newArrayList(replacement, gatewayReplacement);
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(converted);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);
        List<InitiatorParty> configurationInitiatorPartyList = Lists.newArrayList(configurationInitiatorParty);

        new Expectations() {{
            process.getName(); result = "process_1";
            configurationProcess.getName(); result = "process_1";
            gatewayReplacement.getName(); result = "gatewayParty";
            replacement.getName(); result = "replacementParty";
            converted.getName(); result = "gatewayParty"; // update the gateway party
            configurationInitiatorParty.getName(); result = "gatewayParty";

            configurationProcess.getInitiatorPartiesXml(); result = configurationInitiatorParties;
            configurationInitiatorParties.getInitiatorParty(); result = configurationInitiatorPartyList;

            gatewayReplacement.getProcessesWithPartyAsInitiator(); result = Lists.newArrayList(process);
            replacement.getProcessesWithPartyAsInitiator(); result = Lists.newArrayList(process);

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
            configurationBusinessProcesses.getProcesses(); result = Lists.newArrayList(configurationProcess);
        }};

        // When
        partyService.replaceParties(replacements, configuration);

        // Then
        Assert.assertEquals("Should have added the initiator party to the configuration process if not already present",
                2, configurationInitiatorPartyList.size());
    }

    @Test
    public void clearsTheInitiatorPartiesForTheConfigurationProcessIfTheReplacementPartyIsNotSetAsInitiatorWhenReplacingParties(@Injectable Party gatewayReplacement,
            @Injectable eu.domibus.common.model.configuration.Process configurationProcess,
            @Injectable InitiatorParties configurationInitiatorParties,
            @Injectable eu.domibus.common.model.configuration.Party converted) {
        // Given
        List<Party> replacements = Lists.newArrayList(gatewayReplacement);
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(converted);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);

        new Expectations() {{
            converted.getName(); result = "gatewayParty"; // update the gateway party

            configurationProcess.getInitiatorPartiesXml(); result = configurationInitiatorParties;
            configurationInitiatorParties.getInitiatorParty(); result = Lists.newArrayList();

            gatewayReplacement.getProcessesWithPartyAsInitiator(); result = Lists.newArrayList();

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
            configurationBusinessProcesses.getProcesses(); result = Lists.newArrayList(configurationProcess);
        }};

        // When
        partyService.replaceParties(replacements, configuration);

        // Then
        Assert.assertTrue("Should have cleared the initiator party to the configuration process if replacement party not set as its initiator",
                configurationInitiatorParties.getInitiatorParty().isEmpty());
    }

    @Test
    public void initializesTheInitiatorPartiesIfUndefinedForTheConfigurationProcessWhenReplacingParties(@Injectable Party gatewayReplacement,
            @Injectable eu.domibus.common.model.configuration.Process configurationProcess,
            @Injectable InitiatorParties configurationInitiatorParties,
            @Injectable eu.domibus.common.model.configuration.Party converted) {
        // Given
        List<Party> replacements = Lists.newArrayList(gatewayReplacement);
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(converted);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);

        new Expectations() {{
            converted.getName(); result = "gatewayParty"; // update the gateway party

            configurationProcess.getInitiatorPartiesXml(); returns(null, configurationInitiatorParties);
            configurationInitiatorParties.getInitiatorParty(); result = Lists.newArrayList();

            gatewayReplacement.getProcessesWithPartyAsInitiator(); result = Lists.newArrayList();

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
            configurationBusinessProcesses.getProcesses(); result = Lists.newArrayList(configurationProcess);
        }};

        // When
        partyService.replaceParties(replacements, configuration);

        // Then
        new Verifications() {{
            configurationProcess.setInitiatorPartiesXml(withInstanceLike(new InitiatorParties()));
        }};
    }

    @Test
    public void removesResponderPartiesFromProcessConfigurationIfThePartiesBeingReplacedDoNotBelongToThoseProcessesAnymoreWhenReplacingParties(@Injectable Party replacement,
           @Injectable Process process,
           @Injectable eu.domibus.common.model.configuration.Process configurationProcess,
           @Injectable ResponderParties configurationResponderParties,
           @Injectable ResponderParty configurationResponderParty,
           @Injectable eu.domibus.common.model.configuration.Party converted) {
        // Given
        List<Party> replacements = Lists.newArrayList(replacement);
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(converted);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);
        List<ResponderParty> configurationResponderPartyList = Lists.newArrayList(configurationResponderParty);

        new Expectations() {{
            process.getName(); result = "process_1";
            configurationProcess.getName(); result = "process_1";
            replacement.getName(); result = "gatewayParty";
            converted.getName(); result = "gatewayParty"; // update the gateway party
            configurationResponderParty.getName(); result = "configurationParty";

            configurationProcess.getResponderPartiesXml(); result = configurationResponderParties;
            configurationResponderParties.getResponderParty(); result = configurationResponderPartyList;

            replacement.getProcessesWithPartyAsResponder(); result = Lists.newArrayList(process);

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
            configurationBusinessProcesses.getProcesses(); result = Lists.newArrayList(configurationProcess);
        }};

        // When
        partyService.replaceParties(replacements, configuration);

        // Then
        new Verifications() {{
            configurationResponderPartyList.remove(configurationResponderParty);
        }};
    }

    @Test
    public void doesNotAddResponderPartiesIfAlreadyExistingInsideProcessConfigurationWhenReplacingParties(@Injectable Party replacement,
            @Injectable Process process,
            @Injectable eu.domibus.common.model.configuration.Process configurationProcess,
            @Injectable ResponderParties configurationResponderParties,
            @Injectable ResponderParty configurationResponderParty,
            @Injectable eu.domibus.common.model.configuration.Party converted) {
        // Given
        List<Party> replacements = Lists.newArrayList(replacement);
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(converted);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);
        List<ResponderParty> configurationResponderPartyList = Lists.newArrayList(configurationResponderParty);

        new Expectations() {{
            process.getName(); result = "process_1";
            configurationProcess.getName(); result = "process_1";
            replacement.getName(); result = "gatewayParty";
            converted.getName(); result = "gatewayParty"; // update the gateway party
            configurationResponderParty.getName(); result = "gatewayParty";

            configurationProcess.getResponderPartiesXml(); result = configurationResponderParties;
            configurationResponderParties.getResponderParty(); result = configurationResponderPartyList;

            replacement.getProcessesWithPartyAsResponder(); result = Lists.newArrayList(process);

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
            configurationBusinessProcesses.getProcesses(); result = Lists.newArrayList(configurationProcess);
        }};

        // When
        partyService.replaceParties(replacements, configuration);

        // Then
        Assert.assertEquals("Should have not added the responder party to the configuration process if already present",
                1, configurationResponderPartyList.size());
    }


    @Test
    public void addsResponderPartiesIfMissingInsideProcessConfigurationWhenReplacingParties(@Injectable Party gatewayReplacement,
            @Injectable Party replacement,
            @Injectable Process process,
            @Injectable eu.domibus.common.model.configuration.Process configurationProcess,
            @Injectable ResponderParties configurationResponderParties,
            @Injectable ResponderParty configurationResponderParty,
            @Injectable eu.domibus.common.model.configuration.Party converted) {
        // Given
        List<Party> replacements = Lists.newArrayList(replacement, gatewayReplacement);
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(converted);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);
        List<ResponderParty> configurationResponderPartyList = Lists.newArrayList(configurationResponderParty);

        new Expectations() {{
            process.getName(); result = "process_1";
            configurationProcess.getName(); result = "process_1";
            gatewayReplacement.getName(); result = "gatewayParty";
            replacement.getName(); result = "replacementParty";
            converted.getName(); result = "gatewayParty"; // update the gateway party
            configurationResponderParty.getName(); result = "gatewayParty";

            configurationProcess.getResponderPartiesXml(); result = configurationResponderParties;
            configurationResponderParties.getResponderParty(); result = configurationResponderPartyList;

            gatewayReplacement.getProcessesWithPartyAsResponder(); result = Lists.newArrayList(process);
            replacement.getProcessesWithPartyAsResponder(); result = Lists.newArrayList(process);

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
            configurationBusinessProcesses.getProcesses(); result = Lists.newArrayList(configurationProcess);
        }};

        // When
        partyService.replaceParties(replacements, configuration);

        // Then
        Assert.assertEquals("Should have added the responder party to the configuration process if not already present",
                2, configurationResponderPartyList.size());
    }

    @Test
    public void clearsTheResponderPartiesForTheConfigurationProcessIfTheReplacementPartyIsNotSetAsResponderWhenReplacingParties(@Injectable Party gatewayReplacement,
            @Injectable eu.domibus.common.model.configuration.Process configurationProcess,
            @Injectable ResponderParties configurationResponderParties,
            @Injectable eu.domibus.common.model.configuration.Party converted) {
        // Given
        List<Party> replacements = Lists.newArrayList(gatewayReplacement);
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(converted);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);

        new Expectations() {{
            converted.getName(); result = "gatewayParty"; // update the gateway party

            configurationProcess.getResponderPartiesXml(); result = configurationResponderParties;
            configurationResponderParties.getResponderParty(); result = Lists.newArrayList();

            gatewayReplacement.getProcessesWithPartyAsResponder(); result = Lists.newArrayList();

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
            configurationBusinessProcesses.getProcesses(); result = Lists.newArrayList(configurationProcess);
        }};

        // When
        partyService.replaceParties(replacements, configuration);

        // Then
        Assert.assertTrue("Should have cleared the responder party to the configuration process if replacement party not set as its responder",
                configurationResponderParties.getResponderParty().isEmpty());
    }

    @Test
    public void initializesTheResponderPartiesIfUndefinedForTheConfigurationProcessWhenReplacingParties(@Injectable Party gatewayReplacement,
            @Injectable eu.domibus.common.model.configuration.Process configurationProcess,
            @Injectable ResponderParties configurationResponderParties,
            @Injectable eu.domibus.common.model.configuration.Party converted) {
        // Given
        List<Party> replacements = Lists.newArrayList(gatewayReplacement);
        List<eu.domibus.common.model.configuration.Party> convertedForReplacement = Lists.newArrayList(converted);
        List<eu.domibus.common.model.configuration.Party> configurationPartyList = Lists.newArrayList(gatewayParty);

        new Expectations() {{
            converted.getName(); result = "gatewayParty"; // update the gateway party

            configurationProcess.getResponderPartiesXml(); returns(null, configurationResponderParties);
            configurationResponderParties.getResponderParty(); result = Lists.newArrayList();

            gatewayReplacement.getProcessesWithPartyAsResponder(); result = Lists.newArrayList();

            domainCoreConverter.convert(replacements, eu.domibus.common.model.configuration.Party.class); result = convertedForReplacement;

            configurationParties.getParty(); result = configurationPartyList;
            configurationBusinessProcesses.getProcesses(); result = Lists.newArrayList(configurationProcess);
        }};

        // When
        partyService.replaceParties(replacements, configuration);

        // Then
        new Verifications() {{
            configurationProcess.setResponderPartiesXml(withInstanceLike(new ResponderParties()));
        }};
    }

    @Test
    public void throwsExceptionIfItCannotRetrieveThePModeRawConfigurationsArchiveWhenUpdatingParties() {
        // Given
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Could not update PMode parties: PMode not found!");

        new Expectations() {{
           pModeProvider.getRawConfigurationList(); result = Lists.newArrayList();
        }};

        // When
        partyService.updateParties(Lists.newArrayList(), Maps.newHashMap());
    }

    @Test
    public void throwsExceptionIfItCannotRetrieveThePModeConfigurationWhenUpdatingParties(@Injectable PModeArchiveInfo pModeArchiveInfo,
             @Injectable ConfigurationRaw rawConfiguration) throws Exception {
        // Given
        thrown.expect(IllegalStateException.class);

        new Expectations() {{
            pModeArchiveInfo.getId(); result = anyInt;
            rawConfiguration.getXml(); result = any;

            pModeProvider.getRawConfigurationList(); result = Lists.newArrayList(pModeArchiveInfo);
            pModeProvider.getRawConfiguration(anyInt); result = rawConfiguration;
            pModeProvider.getPModeConfiguration((byte[])any); result = new XmlProcessingException("");
        }};

        // When
        partyService.updateParties(Lists.newArrayList(), Maps.newHashMap());
    }

    @Test
    public void throwsExceptionIfItCannotUpdatePModeConfigurationWhenUpdatingParties(@Injectable PModeArchiveInfo pModeArchiveInfo,
              @Injectable ConfigurationRaw rawConfiguration) throws Exception {
        // Given
        thrown.expect(IllegalStateException.class);

        new Expectations() {{
            pModeArchiveInfo.getId(); result = anyInt;
            rawConfiguration.getXml(); result = any;

            pModeProvider.getRawConfigurationList(); result = Lists.newArrayList(pModeArchiveInfo);
            pModeProvider.getRawConfiguration(anyInt); result = rawConfiguration;
            pModeProvider.getPModeConfiguration((byte[]) any); result = configuration;

            partyService.replaceParties((List<Party>)any, configuration); result = any;
            rawConfiguration.getConfigurationDate(); result = new Date();
            pModeProvider.serializePModeConfiguration(configuration); result = any;
            pModeProvider.updatePModes((byte[]) any, anyString); result = new XmlProcessingException("");

        }};

        // When
        partyService.updateParties(Lists.newArrayList(), Maps.newHashMap());
    }

    @Test
    public void removesCertificatesInTheCurrentDomainForRemovedPartiesWhenUpdatingParties(@Injectable PModeArchiveInfo pModeArchiveInfo,
             @Injectable ConfigurationRaw rawConfiguration,
             @Injectable eu.domibus.common.model.configuration.Party removedParty) throws Exception {
        // Given
        List<eu.domibus.common.model.configuration.Party> removedParties = Lists.newArrayList(removedParty);
        new Expectations(partyService) {{
            partyService.replaceParties((List<Party>) any, configuration); result = new PartyServiceImpl.ReplacementResult(configuration, removedParties);
        }};

        new Expectations() {{
            pModeArchiveInfo.getId(); result = anyInt;
            rawConfiguration.getXml(); result = any;
            removedParty.getName(); result = "removed";

            pModeProvider.getRawConfigurationList(); result = Lists.newArrayList(pModeArchiveInfo);
            pModeProvider.getRawConfiguration(anyInt); result = rawConfiguration;
            pModeProvider.getPModeConfiguration((byte[]) any); result = configuration;

            rawConfiguration.getConfigurationDate(); result = new Date();
            pModeProvider.serializePModeConfiguration(configuration); result = any;
            pModeProvider.updatePModes((byte[]) any, withPrefix("Updated parties to version of"));
        }};

        // When
        partyService.updateParties(Lists.newArrayList(), Maps.newHashMap());

        // Then
        new Verifications() {{
            List<String> aliases;
            multiDomainCertificateProvider.removeCertificate(currentDomain, aliases = withCapture());
            Assert.assertEquals("Should have removed the certificate in the current domain for the removed parties",
                    Lists.newArrayList("removed"), aliases);
        }};
    }

    @Test
    public void ignoresNullPartyCertificatesInTheCurrentDomainWhenUpdatingParties(@Injectable PModeArchiveInfo pModeArchiveInfo,
              @Injectable ConfigurationRaw rawConfiguration,
              @Injectable eu.domibus.common.model.configuration.Party removedParty) throws Exception {
        // Given
        List<eu.domibus.common.model.configuration.Party> removedParties = Lists.newArrayList(removedParty);
        Map<String, String> partyToCertificateMap = Maps.newHashMap();
        partyToCertificateMap.put("party_1", null);

        new Expectations(partyService) {{
            partyService.replaceParties((List<Party>) any, configuration); result = new PartyServiceImpl.ReplacementResult(configuration, removedParties);
        }};

        new Expectations() {{
            pModeArchiveInfo.getId(); result = anyInt;
            rawConfiguration.getXml(); result = any;
            removedParty.getName(); result = "removed";

            pModeProvider.getRawConfigurationList(); result = Lists.newArrayList(pModeArchiveInfo);
            pModeProvider.getRawConfiguration(anyInt); result = rawConfiguration;
            pModeProvider.getPModeConfiguration((byte[]) any); result = configuration;

            rawConfiguration.getConfigurationDate(); result = new Date();
            pModeProvider.serializePModeConfiguration(configuration); result = any;
            pModeProvider.updatePModes((byte[]) any, withPrefix("Updated parties to version of"));
            multiDomainCertificateProvider.removeCertificate(currentDomain, (List<String>) any);
        }};

        // When
        partyService.updateParties(Lists.newArrayList(), partyToCertificateMap);

        // Then
        new Verifications() {{
            List<CertificateEntry> certificates;
            multiDomainCertificateProvider.addCertificate(currentDomain, certificates = withCapture(), true);
            Assert.assertTrue("Should have ignore party certificates that are null when updating parties",
                    certificates.isEmpty());
        }};
    }

    @Test
    public void addsPartyCertificatesInTheCurrentDomainWhenUpdatingParties(@Injectable PModeArchiveInfo pModeArchiveInfo,
               @Injectable ConfigurationRaw rawConfiguration,
               @Injectable X509Certificate x509Certificate,
               @Injectable eu.domibus.common.model.configuration.Party removedParty) throws Exception {
        // Given
        List<eu.domibus.common.model.configuration.Party> removedParties = Lists.newArrayList(removedParty);
        Map<String, String> partyToCertificateMap = Maps.newHashMap();
        partyToCertificateMap.put("party_1", "certificate_1");

        new Expectations(partyService) {{
            partyService.replaceParties((List<Party>) any, configuration); result = new PartyServiceImpl.ReplacementResult(configuration, removedParties);
        }};

        new Expectations() {{
            pModeArchiveInfo.getId(); result = anyInt;
            rawConfiguration.getXml(); result = any;
            removedParty.getName(); result = "removed";

            pModeProvider.getRawConfigurationList(); result = Lists.newArrayList(pModeArchiveInfo);
            pModeProvider.getRawConfiguration(anyInt); result = rawConfiguration;
            pModeProvider.getPModeConfiguration((byte[]) any); result = configuration;

            rawConfiguration.getConfigurationDate(); result = new Date();
            pModeProvider.serializePModeConfiguration(configuration); result = any;
            pModeProvider.updatePModes((byte[]) any, withPrefix("Updated parties to version of"));
            multiDomainCertificateProvider.removeCertificate(currentDomain, (List<String>) any);
            certificateService.loadCertificateFromString("certificate_1"); result = x509Certificate;
        }};

        // When
        partyService.updateParties(Lists.newArrayList(), partyToCertificateMap);

        // Then
        new Verifications() {{
            List<CertificateEntry> certificates;
            multiDomainCertificateProvider.addCertificate(currentDomain, certificates = withCapture(), true);
            Assert.assertTrue("Should have ignore party certificates that are null when updating parties",
                    certificates.size() == 1
                            && "party_1".equals(certificates.get(0).getAlias())
                            && x509Certificate == certificates.get(0).getCertificate());
        }};
    }


    @Test
    public void throwsExceptionIfLoadingPartyCertificatesFailsInTheCurrentDomainWhenUpdatingParties(@Injectable PModeArchiveInfo pModeArchiveInfo,
               @Injectable ConfigurationRaw rawConfiguration,
               @Injectable eu.domibus.common.model.configuration.Party removedParty) throws Exception {
        // Given
        thrown.expect(IllegalStateException.class);

        List<eu.domibus.common.model.configuration.Party> removedParties = Lists.newArrayList(removedParty);
        Map<String, String> partyToCertificateMap = Maps.newHashMap();
        partyToCertificateMap.put("party_1", "certificate_1");

        new Expectations(partyService) {{
            partyService.replaceParties((List<Party>) any, configuration); result = new PartyServiceImpl.ReplacementResult(configuration, removedParties);
        }};

        new Expectations() {{
            pModeArchiveInfo.getId(); result = anyInt;
            rawConfiguration.getXml(); result = any;
            removedParty.getName(); result = "removed";

            pModeProvider.getRawConfigurationList(); result = Lists.newArrayList(pModeArchiveInfo);
            pModeProvider.getRawConfiguration(anyInt); result = rawConfiguration;
            pModeProvider.getPModeConfiguration((byte[]) any); result = configuration;

            rawConfiguration.getConfigurationDate(); result = new Date();
            pModeProvider.serializePModeConfiguration(configuration); result = any;
            pModeProvider.updatePModes((byte[]) any, withPrefix("Updated parties to version of"));
            multiDomainCertificateProvider.removeCertificate(currentDomain, (List<String>) any);
            certificateService.loadCertificateFromString("certificate_1"); result = new CertificateException();
        }};

        // When
        partyService.updateParties(Lists.newArrayList(), partyToCertificateMap);
    }
}
