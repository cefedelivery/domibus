package eu.domibus.ebms3.common.dao;

import eu.domibus.api.xml.XMLUtil;
import eu.domibus.common.dao.ConfigurationDAO;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Mpc;
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
    public void testIsMpcExistant() {
        final Set<Mpc> testMpc = loadTestMpcs();
        new Expectations() {{
            cachingPModeProvider.getConfiguration().getMpcs();
            result = testMpc;
        }};

        Assert.assertEquals(Boolean.TRUE, cachingPModeProvider.isMpcExistant(DEFAULTMPC));
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

        Assert.assertEquals(10, cachingPModeProvider.getRetentionDownloadedByMpcName(ANOTHERMPC));
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

        Assert.assertEquals(12400, cachingPModeProvider.getRetentionUndownloadedByMpcName(ANOTHERMPC));
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

    private Set<Mpc> loadTestMpcs() {
        final Set<Mpc> testMpc = new HashSet();
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
}
