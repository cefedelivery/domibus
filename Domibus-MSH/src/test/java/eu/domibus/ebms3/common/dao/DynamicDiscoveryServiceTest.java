package eu.domibus.ebms3.common.dao;

import eu.domibus.common.exception.ConfigurationException;
import no.difi.vefa.edelivery.lookup.model.ProcessIdentifier;
import no.difi.vefa.edelivery.lookup.model.ServiceMetadata;
import no.difi.vefa.edelivery.lookup.model.TransportProfile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

import static org.mockito.Mockito.doReturn;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:eu/domibus/ebms3/common/dao/DynamicDiscoveryServiceTest/DynamicDiscoveryServiceTest-context.xml")
@DirtiesContext
public class DynamicDiscoveryServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private DynamicDiscoveryService dynamicDiscoveryService;

    @Mock
    private Properties domibusProperties;

    @Mock
    private ServiceMetadata serviceMetadata;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLookupInformation_SMLZoneMissing_ExceptionExpected() throws Exception {
        thrown.expect(ConfigurationException.class);

        doReturn(null).when(domibusProperties).getProperty(DynamicDiscoveryService.SMLZONE_KEY);

        dynamicDiscoveryService.lookupInformation(null, null, null, null, null);
    }

    @Test
    public void testLookupInformation_NoEndpointFound_ExceptionExpected() throws Exception {
        thrown.expect(ConfigurationException.class);

        doReturn(null).when(serviceMetadata).getEndpoint(Matchers.<ProcessIdentifier>any(), Matchers.<TransportProfile>any());

        dynamicDiscoveryService.lookupInformation(null, null, null, null, null);
    }

}