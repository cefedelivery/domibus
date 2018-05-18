package eu.domibus.common.dao;

import com.google.common.collect.Lists;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.*;
import eu.domibus.common.model.configuration.Process;
import eu.domibus.ebms3.common.model.Ebms3Constants;
import eu.domibus.ebms3.common.validators.ConfigurationValidator;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBContext;
import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@RunWith(JMockit.class)
public class PModeDaoTest {

    @Injectable
    protected EntityManager entityManager;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    private JAXBContext jaxbContextConfig;

    @Injectable
    private JmsOperations jmsTemplateCommand;

    @Injectable
    XMLUtil xmlUtil;

    @Injectable
    List<ConfigurationValidator> configurationValidators;

    @Injectable
    protected ProcessDao processDao;

    @Injectable
    ConfigurationRawDAO configurationRawDAO;

    @Tested(fullyInitialized = true)
    private PModeDao pModeDao;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Mocked
    TypedQuery<LegConfiguration> queryLegConfiguration;

    @Test
    public void testFindPushLegName() throws EbMS3Exception {

        new Expectations(pModeDao) {{
            pModeDao.findLegNameMepBindingAgnostic("agreementName", "senderParty", "receiverParty", "service", "action");
            result = "pushLeg";
        }};
        final String legName = pModeDao.findLegName("agreementName", "senderParty", "receiverParty", "service", "action");
        Assert.assertEquals("pushLeg", legName);
    }

    @Test
    public void testFindPushNotFoundPullNotFoundLegName() throws EbMS3Exception {
        new Expectations(pModeDao) {{
            pModeDao.findLegNameMepBindingAgnostic("agreementName", "senderParty", "receiverParty", "service", "action");
            result = new EbMS3Exception(null, "", "", null);
            pModeDao.findLegNameMepBindingAgnostic("agreementName", "receiverParty", "senderParty", "service", "action");
            result = new EbMS3Exception(null, "", "", null);
        }};
        EbMS3Exception check = null;
        try {
            pModeDao.findLegName("agreementName", "senderParty", "receiverParty", "service", "action");
            Assert.assertTrue(false);
        } catch (EbMS3Exception e) {
            check = e;
        }
        Assert.assertNotNull(check);
    }

    @Test
    public void testFindPullLegFoundButNoPullProcess() throws EbMS3Exception {
        final String pullLegFound = "pullLegFound";
        new Expectations(pModeDao) {{
            pModeDao.findLegNameMepBindingAgnostic("agreementName", "senderParty", "receiverParty", "service", "action");
            result = new EbMS3Exception(null, "", "", null);
            pModeDao.findLegNameMepBindingAgnostic("agreementName", "receiverParty", "senderParty", "service", "action");
            result = pullLegFound;
            processDao.findPullProcessByLegName(pullLegFound);
            result = Lists.newArrayList();

        }};
        EbMS3Exception check = null;
        try {
            pModeDao.findLegName("agreementName", "senderParty", "receiverParty", "service", "action");
            Assert.assertTrue(false);
        } catch (EbMS3Exception e) {
            check = e;
        }
        Assert.assertNotNull(check);
    }

    @Test
    public void testFindPullLegFoundAndOnePullProcess() throws EbMS3Exception {
        final String pullLegFound = "pullLegFound";
        new Expectations(pModeDao) {{
            pModeDao.findLegNameMepBindingAgnostic("agreementName", "senderParty", "receiverParty", "service", "action");
            result = new EbMS3Exception(null, "", "", null);
            pModeDao.findLegNameMepBindingAgnostic("agreementName", "receiverParty", "senderParty", "service", "action");
            result = pullLegFound;
            processDao.findPullProcessByLegName(pullLegFound);
            result = Lists.newArrayList(new Process());

        }};

        String legName = pModeDao.findLegName("agreementName", "senderParty", "receiverParty", "service", "action");
        Assert.assertEquals(pullLegFound, legName);

    }

    @Test
    public void testGetPModeFile() {
        // Given
        ConfigurationRaw resultConfigurationRaw = new ConfigurationRaw();
        final byte[] xmlBytes = {1, 0, 1};
        resultConfigurationRaw.setXml(xmlBytes);
        new Expectations(pModeDao) {{
            pModeDao.getRawConfiguration(anyInt);
            result = resultConfigurationRaw;
        }};

        // When
        final byte[] pModeFile = pModeDao.getPModeFile(1);

        // Then
        Assert.assertEquals(pModeFile, xmlBytes);
    }

    @Test
    public void testGetPModeFileNull() {
        // Given
        new Expectations(pModeDao) {{
            pModeDao.getRawConfiguration(anyInt);
            result = null;
        }};

        // When
        final byte[] pModeFile = pModeDao.getPModeFile(1);

        // Then
        Assert.assertEquals(pModeFile.length, 0);
    }

    @Test
    public void testGetRawConfiguration() {
        // Given
        final ConfigurationRaw rawConfiguration = new ConfigurationRaw();
        rawConfiguration.setDescription("description");
        rawConfiguration.setXml(new byte[]{1, 0, 1});
        new Expectations(configurationRawDAO) {{
            configurationRawDAO.getConfigurationRaw(anyInt);
            result = rawConfiguration;
        }};

        // When
        final ConfigurationRaw rawConfigurationGot = pModeDao.getRawConfiguration(1);

        // Then
        Assert.assertEquals(rawConfigurationGot, rawConfiguration);
    }

    @Test
    public void testRemovePMode() {
        // Given
        new Expectations(configurationRawDAO) {{
            configurationRawDAO.deleteById(anyInt);
        }};
        try {
            // When
            pModeDao.removePMode(1);
        } catch(Exception ex) {
            Assert.fail();
        }

        // Then
        // no exception, no fail
    }

    @Test
    public void testGetRawConfigurationList() {
        // Given
        final List<PModeArchiveInfo> detailedConfigurationRawList = new ArrayList<>();
        detailedConfigurationRawList.add(new PModeArchiveInfo(1, new Date(), "username", "description"));
        new Expectations(configurationRawDAO) {{
             configurationRawDAO.getDetailedConfigurationRaw();
             result = detailedConfigurationRawList;
        }};

        // When
        final List<PModeArchiveInfo> rawConfigurationListGot = pModeDao.getRawConfigurationList();

        // Then
        Assert.assertEquals(detailedConfigurationRawList, rawConfigurationListGot);
    }

    @Test
    public void testFind() throws EbMS3Exception {
        final String STR_TEST = "test";
        Process process = new Process();
        List<Process> processes = new ArrayList<>();
        processes.add(process);

        Party party = new Party();
        Set<Party> partySet = new HashSet<>();
        partySet.add(party);

        Identifier identifier = new Identifier();
        identifier.setPartyId(STR_TEST);
        Set<Identifier> identifierSet = new HashSet<>();
        identifierSet.add(identifier);

        List<String> expectedResult = new ArrayList<>();
        expectedResult.add(STR_TEST);

        new Expectations(process, party) {{
            entityManager.createNamedQuery(anyString, LegConfiguration.class);
            result = queryLegConfiguration;
            processDao.findProcessByLegName(anyString);
            result = processes;
            process.getResponderParties();
            result = partySet;
            party.getIdentifiers();
            result = identifierSet;
        }};

        // When
        List<String> partyIdByServiceAndAction = pModeDao.findPartyIdByServiceAndAction(Ebms3Constants.TEST_SERVICE, Ebms3Constants.TEST_ACTION);

        // Then
        Assert.assertEquals(expectedResult, partyIdByServiceAndAction);
    }

    @Test
    public void testGetPartyIdType() {
        Assert.assertNull(pModeDao.getPartyIdType(""));
    }

    @Test
    public void testGetServiceType() {
        Assert.assertNull(pModeDao.getServiceType(""));
    }

    @Test
    public void testGetRole() {
        Assert.assertNull(pModeDao.getRole("", ""));
    }

    @Test
    public void testGetAgreementRef() {
        Assert.assertNull(pModeDao.getAgreementRef( ""));
    }

}
