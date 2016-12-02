package eu.domibus.ebms3.common.dao;

import eu.domibus.api.xml.XMLUtil;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.model.configuration.*;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Arun Raj
 * @since 3.3
 */
@RunWith(JMockit.class)
public class CachingPModeProviderTest {

    private static final String URI1 = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC";
    private static final String URI2 = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/anotherMpc";
    private static final String DEFAULTMPC = "defaultMpc";
    private static final String ANOTHERMPC = "anotherMpc";
    private static final String NONEXISTANTMPC = "NonExistantMpc";

    @Injectable
    ConfigurationDAO configurationDAO;

    @Injectable
    EntityManager entityManager;

    @Injectable
    JAXBContext jaxbContextConfig;

    @Injectable
    JmsOperations jmsTemplateCommand;

    @Injectable
    XMLUtil xmlUtil;

    @Injectable
    Configuration configuration;

    @Tested
    CachingPModeProvider cachingPModeProvider;

    @Test
    public void loadSamplePModeConfiguration() throws JAXBException {
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream("SamplePModes/domibus-configuration-valid.xml");
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        configuration = (Configuration) unmarshaller.unmarshal(xmlStream);


        Mpcs mpcs = configuration.getMpcsXml();
        System.out.println("mpcs:" + mpcs);
        System.out.println("mpcs size:" + mpcs.getMpc().size());

        configuration.setMpcs(new HashSet<>(configuration.getMpcsXml().getMpc()));
        System.out.println(configuration.getMpcs().size());

        configuration.getBusinessProcesses();
    }

    @Test
    public void testIsMpcExistant() throws JAXBException {
        if (configuration == null) {
            loadSamplePModeConfiguration();
        }
        final Set<Mpc> testMpc = loadTestMpcs();
/*        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};*/

        Mpcs mpcs = configuration.getMpcsXml();
        System.out.println(mpcs);
//        Assert.assertEquals(Boolean.TRUE, cachingPModeProvider.isMpcExistant(DEFAULTMPC.toUpperCase()));
    }

    @Test
    public void testIsMpcExistantNOK() {
        final Set<Mpc> testMpc = loadTestMpcs();
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = testMpc;
        }};

        Assert.assertEquals(Boolean.FALSE, cachingPModeProvider.isMpcExistant(NONEXISTANTMPC));
    }

    @Test
    public void testGetRetentionDownloadedByMpcName() {
        final Set<Mpc> testMpc = loadTestMpcs();
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = testMpc;
        }};

        Assert.assertEquals(10, cachingPModeProvider.getRetentionDownloadedByMpcName(ANOTHERMPC.toLowerCase()));
    }

    @Test
    public void testGetRetentionDownloadedByMpcNameNOK() {
        final Set<Mpc> testMpc = loadTestMpcs();
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = testMpc;
        }};

        Assert.assertEquals(0, cachingPModeProvider.getRetentionDownloadedByMpcName(NONEXISTANTMPC));
    }


    @Test
    public void testGetRetentionUnDownloadedByMpcName() {

        final Set<Mpc> testMpc = loadTestMpcs();
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = testMpc;
        }};

        Assert.assertEquals(12400, cachingPModeProvider.getRetentionUndownloadedByMpcName(ANOTHERMPC.toUpperCase()));
    }

    @Test
    public void testGetRetentionUnDownloadedByMpcNameNOK() {

        final Set<Mpc> testMpc = loadTestMpcs();
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = testMpc;
        }};

        Assert.assertEquals(-1, cachingPModeProvider.getRetentionUndownloadedByMpcName(NONEXISTANTMPC));
    }

    @Test
    public void testGetMpcURIList() {

        final Set<Mpc> testMpc = loadTestMpcs();
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = testMpc;
        }};

        List<String> result = cachingPModeProvider.getMpcURIList();
        Assert.assertEquals(URI2, result.get(0));
        Assert.assertEquals(URI1, result.get(1));
    }


    protected Set<Mpc> loadTestMpcs() {
        Set<Mpc> testMpc = new HashSet();
        Mpc mpc1 = new Mpc();
        mpc1.setName(DEFAULTMPC);
        mpc1.setQualifiedName(URI1);
        mpc1.setEnabled(true);
        mpc1.setDefault(true);
        mpc1.setRetentionDownloaded(0);
        mpc1.setRetentionUndownloaded(14400);
        testMpc.add(mpc1);

        Mpc mpc2 = new Mpc();
        mpc2.setName(ANOTHERMPC);
        mpc2.setQualifiedName(URI2);
        mpc2.setEnabled(true);
        mpc2.setDefault(false);
        mpc2.setRetentionDownloaded(10);
        mpc2.setRetentionUndownloaded(12400);
        testMpc.add(mpc2);

        return testMpc;
    }

    protected BusinessProcesses loadTestBusinessProcesses() {
        BusinessProcesses businessProcesses = new BusinessProcesses();
        businessProcesses.setRoles(loadTestRoles());
        businessProcesses.setPartyIdTypes(loadTestPartyIdTypes());
        businessProcesses.setParties(loadTestParties(businessProcesses.getPartyIdTypes()));
        businessProcesses.setMeps(loadTestMeps());
        businessProcesses.setMepBindings(loadTestMepBindings());
        return businessProcesses;
    }

    private Set<Role> loadTestRoles() {
        Set<Role> testRole = new HashSet();

        Role role1 = new Role();
        role1.setName("defaultInitiatorRole");
        role1.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator");
        testRole.add(role1);

        Role role2 = new Role();
        role2.setName("defaultResponderRole");
        role2.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder");
        return testRole;
    }

    private Set<Party> loadTestParties(Set<PartyIdType> partyIdTypeSet) {
        Set<Party> testPartySet = new HashSet<>();
        for (PartyIdType partyIdType : partyIdTypeSet) {
            if ("partyTypeUrn".equalsIgnoreCase(partyIdType.getName())) {
                testPartySet.add(loadTestParty("red_gw", partyIdType));
                testPartySet.add(loadTestParty("blue_gw", partyIdType));
            }
        }
        return testPartySet;
    }

    private Set<PartyIdType> loadTestPartyIdTypes() {
        Set<PartyIdType> testPartyIdTypeSet = new HashSet<>();

        PartyIdType partyIdType1 = new PartyIdType();
        partyIdType1.setName("partyTypeUrn");
        partyIdType1.setValue("urn:oasis:names:tc:ebcore:partyid-type:unregistered");
        testPartyIdTypeSet.add(partyIdType1);

        PartyIdType partyIdType2 = new PartyIdType();
        partyIdType2.setName("IncorrectPartyTypeUrn");
        partyIdType2.setValue("IncorrectURN");
        testPartyIdTypeSet.add(partyIdType2);

        return testPartyIdTypeSet;
    }

    private Party loadTestParty(String partyName, PartyIdType partyIdType) {
        Party party = new Party();

        switch (partyName) {
            case "red_gw": {
                party.setName(partyName);
                party.setEndpoint("http://localhost:9080/domibus/services/msh");

                Identifier identifier = new Identifier();
                identifier.setPartyId("domibus-red");
                identifier.setPartyIdType(partyIdType);
                party.getIdentifiers().add(identifier);
                break;
            }
            case "blue_gw":
            default: {
                party.setName(partyName);
                party.setEndpoint("http://localhost:8081/domibus/services/msh");

                Identifier identifier = new Identifier();
                identifier.setPartyId("domibus-blue");
                identifier.setPartyIdType(partyIdType);
                party.getIdentifiers().add(identifier);
                break;
            }
        }
        return party;
    }

    private Set<Mep> loadTestMeps() {
        Set<Mep> testMepSet = new HashSet<>();
        testMepSet.add(loadTestMep("oneway"));
        testMepSet.add(loadTestMep("twoway"));
        return testMepSet;
    }

    private Mep loadTestMep(String messageExchangePattern) {
        Mep testMep = new Mep();
        testMep.setName(messageExchangePattern);
        switch (messageExchangePattern) {
            case "oneway": {
                testMep.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/oneWay");
                break;
            }
            case "twoway":
            default: {
                testMep.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/twoWay");
                break;
            }
        }
        return testMep;
    }

    private Set<Binding> loadTestMepBindings() {
        Set<Binding> testMepBindingSet = new HashSet<>();
        testMepBindingSet.add(loadTestBinding("push"));
        testMepBindingSet.add(loadTestBinding("pushAndPush"));
        return testMepBindingSet;
    }

    private Binding loadTestBinding(String messageExchgPatternBinding) {
        Binding testBinding = new Binding();
        testBinding.setName(messageExchgPatternBinding);
        switch (messageExchgPatternBinding) {
            case "push": {
                testBinding.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/push");
                break;
            }
            case "pushAndPush":
            default: {
                testBinding.setValue("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/push-and-push");
                break;
            }
        }
        return testBinding;
    }

}
