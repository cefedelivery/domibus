package eu.domibus.core.crypto;

import com.google.common.collect.Lists;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.crypto.spi.Domain;
import eu.domibus.core.crypto.spi.DomainCryptoServiceSpi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

@RunWith(MockitoJUnitRunner.class)
public class DomainCryptoServiceImplTest {

    @Mock
    private DomibusPropertyProvider domibusPropertyProvider;

    @Mock
    private eu.domibus.api.multitenancy.Domain domain;

    @InjectMocks
    private DomainCryptoServiceImpl domainCryptoService;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void init() {
        final String dss = "DSS";
        final DomainCryptoServiceSpi defaultSpi = Mockito.mock(DomainCryptoServiceSpi.class);
        final DomainCryptoServiceSpi dssSpi = Mockito.mock(DomainCryptoServiceSpi.class);
        when(defaultSpi.getIdentifier()).thenReturn("DEFAULT");
        when(dssSpi.getIdentifier()).thenReturn(dss);
        when(domain.getCode()).thenReturn("DEF");
        when(domain.getName()).thenReturn("DEFAULT");
        domainCryptoService.setDomainCryptoServiceSpiList(Lists.newArrayList(defaultSpi,dssSpi));
        when(domibusPropertyProvider.getDomainProperty(domainCryptoService.IAM_IDENTIFIER)).thenReturn(dss);
        domainCryptoService.init();
        verify(dssSpi,times(1)).setDomain(new Domain("DEF","DEFAULT"));
        verify(dssSpi,times(1)).init();
    }

    @Test(expected = IllegalStateException.class)
    public void initTooManyProviderForGivenIdentifier() {
        final String dss = "DSS";
        final DomainCryptoServiceSpi defaultSpi = Mockito.mock(DomainCryptoServiceSpi.class);
        final DomainCryptoServiceSpi dssSpi = Mockito.mock(DomainCryptoServiceSpi.class);
        when(defaultSpi.getIdentifier()).thenReturn(dss);
        when(dssSpi.getIdentifier()).thenReturn(dss);
        domainCryptoService.setDomainCryptoServiceSpiList(Lists.newArrayList(defaultSpi,dssSpi));
        when(domibusPropertyProvider.getDomainProperty(domainCryptoService.IAM_IDENTIFIER)).thenReturn(dss);
        domainCryptoService.init();
    }

    @Test(expected = IllegalStateException.class)
    public void initNoProviderCorrespondToIdentifier() {
        final String dss = "DSS";
        final DomainCryptoServiceSpi defaultSpi = Mockito.mock(DomainCryptoServiceSpi.class);
        final DomainCryptoServiceSpi dssSpi = Mockito.mock(DomainCryptoServiceSpi.class);
        when(defaultSpi.getIdentifier()).thenReturn(dss);
        when(dssSpi.getIdentifier()).thenReturn(dss);
        domainCryptoService.setDomainCryptoServiceSpiList(Lists.newArrayList());
        when(domibusPropertyProvider.getDomainProperty(domainCryptoService.IAM_IDENTIFIER)).thenReturn(dss);
        domainCryptoService.init();
    }
}