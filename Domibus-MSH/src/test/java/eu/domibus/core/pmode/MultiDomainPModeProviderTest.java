package eu.domibus.core.pmode;

import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.dao.ConfigurationRawDAO;
import eu.domibus.common.dao.ProcessDao;
import eu.domibus.ebms3.common.validators.ConfigurationValidator;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.Topic;
import javax.persistence.EntityManager;
import javax.xml.bind.JAXBContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class MultiDomainPModeProviderTest {

    protected volatile Map<Domain, PModeProvider> providerMap = new HashMap<>();

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected PModeProviderFactoryImpl pModeProviderFactory;

    @Injectable
    protected ConfigurationDAO configurationDAO;

    @Injectable
    protected ConfigurationRawDAO configurationRawDAO;

    @Injectable
    protected EntityManager entityManager;

    @Injectable
    private JAXBContext jaxbContextConfig;

    @Injectable
    protected JMSManager jmsManager;

    @Injectable
    protected Topic clusterCommandTopic;

    @Injectable
    XMLUtil xmlUtil;

    @Injectable
    List<ConfigurationValidator> configurationValidators;

    @Injectable
    protected ProcessDao processDao;

    @Tested
    MultiDomainPModeProvider multiDomainPModeProvider;

    @Test
    public void testGetCurrentPModeProvider(@Injectable Domain currentDomain, @Injectable PModeProvider pModeProvider) {
        multiDomainPModeProvider.providerMap = providerMap;

        new Expectations() {{
            domainContextProvider.getCurrentDomain();
            result = currentDomain;

            pModeProviderFactory.createDomainPModeProvider(currentDomain);
            result = pModeProvider;
        }};

        multiDomainPModeProvider.getCurrentPModeProvider();
        Assert.assertTrue(providerMap.containsKey(currentDomain));

        multiDomainPModeProvider.getCurrentPModeProvider();

        new Verifications() {{
            pModeProviderFactory.createDomainPModeProvider(currentDomain);
            times = 1;
        }};


    }
}
