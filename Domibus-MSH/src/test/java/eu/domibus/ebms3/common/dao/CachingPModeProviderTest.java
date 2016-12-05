package eu.domibus.ebms3.common.dao;

import eu.domibus.api.xml.XMLUtil;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.model.configuration.Configuration;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;

import javax.persistence.EntityManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Arun Raj
 * @since 3.3
 */
@RunWith(JMockit.class)
public class CachingPModeProviderTest {

    private static final Log LOG = LogFactory.getLog(CachingPModeProviderTest.class);

    private static final String VALID_PMODE_CONFIG_URI = "SamplePModes/domibus-configuration-valid.xml";
    private static final String URI1 = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMpc";
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


    public Configuration loadSamplePModeConfiguration(String samplePModeFileRelativeURI) throws JAXBException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        LOG.debug("Inside sample PMode configuration");
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream(samplePModeFileRelativeURI);
        JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        configuration = (Configuration) unmarshaller.unmarshal(xmlStream);
        Method m = configuration.getClass().getDeclaredMethod("preparePersist", null);
        m.setAccessible(true);
        m.invoke(configuration);

        return configuration;
    }

    @Test
    public void testIsMpcExistant() throws JAXBException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);

        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(Boolean.TRUE, cachingPModeProvider.isMpcExistant(DEFAULTMPC.toUpperCase()));
    }

    @Test
    public void testIsMpcExistantNOK() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(Boolean.FALSE, cachingPModeProvider.isMpcExistant(NONEXISTANTMPC));
    }

    @Test
    public void testGetRetentionDownloadedByMpcName() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(3, cachingPModeProvider.getRetentionDownloadedByMpcName(ANOTHERMPC.toLowerCase()));
    }

    @Test
    public void testGetRetentionDownloadedByMpcNameNOK() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(0, cachingPModeProvider.getRetentionDownloadedByMpcName(NONEXISTANTMPC));
    }


    @Test
    public void testGetRetentionUnDownloadedByMpcName() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(5, cachingPModeProvider.getRetentionUndownloadedByMpcName(ANOTHERMPC.toUpperCase()));
    }

    @Test
    public void testGetRetentionUnDownloadedByMpcNameNOK() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        Assert.assertEquals(-1, cachingPModeProvider.getRetentionUndownloadedByMpcName(NONEXISTANTMPC));
    }

    @Test
    public void testGetMpcURIList() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JAXBException {
        configuration = loadSamplePModeConfiguration(VALID_PMODE_CONFIG_URI);
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = configuration.getMpcs();
        }};

        List<String> result = cachingPModeProvider.getMpcURIList();
        Assert.assertEquals(URI2, result.get(0));
        Assert.assertEquals(URI1, result.get(1));
    }

}
