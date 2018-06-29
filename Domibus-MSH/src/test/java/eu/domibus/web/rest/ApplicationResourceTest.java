package eu.domibus.web.rest;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.web.rest.ro.DomainRO;
import eu.domibus.web.rest.ro.DomibusInfoRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class ApplicationResourceTest {

    private static final String DOMIBUS_VERSION = "Domibus Unit Tests";
    private static final String DOMIBUS_CUSTOMIZED_NAME = "Domibus Customized Name";

    @Tested
    ApplicationResource applicationResource;

    @Injectable
    DomibusPropertiesService domibusPropertiesService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    DomainService domainService;

    @Injectable
    DomainCoreConverter domainCoreConverter;

    @Test
    public void testGetDomibusInfo() throws Exception {
        // Given
        new Expectations() {{
            domibusPropertiesService.getDisplayVersion();
            result = DOMIBUS_VERSION;
        }};

        // When
        DomibusInfoRO domibusInfo = applicationResource.getDomibusInfo();

        // Then
        Assert.assertNotNull(domibusInfo);
        Assert.assertEquals(DOMIBUS_VERSION, domibusInfo.getVersion());
    }

    public void testDomibusName(String name) {
        // Given
        new Expectations(applicationResource) {{
            domibusPropertyProvider.getProperty(ApplicationResource.DOMIBUS_CUSTOM_NAME, ApplicationResource.DOMIBUS_DEFAULTVALUE_NAME);
            result = name;
        }};

        // When
        final String domibusName = applicationResource.getDomibusName();

        // Then
        Assert.assertEquals(name, domibusName);
    }

    @Test
    public void testGetDomibusCustomName() {
        testDomibusName(DOMIBUS_CUSTOMIZED_NAME);
    }

    @Test
    public void testGetDomibusDefaultName() {
        testDomibusName(ApplicationResource.DOMIBUS_DEFAULTVALUE_NAME);
    }

    @Test
    public void testGetDomains() {
        // Given
        final List<Domain> domainEntries = Arrays.asList(DomainService.DEFAULT_DOMAIN);
        final DomainRO domainRO = new DomainRO();
        domainRO.setCode(DomainService.DEFAULT_DOMAIN.getCode());
        domainRO.setName(DomainService.DEFAULT_DOMAIN.getName());
        final List<DomainRO> domainROEntries = Arrays.asList(domainRO);

        new Expectations(applicationResource) {{
            domainService.getDomains();
            result = domainEntries;

            domainCoreConverter.convert(domainEntries, DomainRO.class);
            result = domainROEntries;
        }};

        // When
        final List<DomainRO> result = applicationResource.getDomains();

        // Then
        Assert.assertNotNull(result);
        Assert.assertNotEquals(0, result.size());
        Assert.assertEquals(domainROEntries, result);
    }

    @Test
    public void testGetMultiTenancy() {
        // Given
        new Expectations(applicationResource) {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
        }};

        // When
        final Boolean isMultiTenancy = applicationResource.getMultiTenancy();

        // Then
        Assert.assertEquals(true, isMultiTenancy);
    }

    @Test
    public void testGetFourCornerEnabled() throws Exception {

        new Expectations() {{
            domibusPropertyProvider.getProperty(ApplicationResource.FOURCORNERMODEL_ENABLED_KEY, anyString);
            result = "false";
        }};

        //tested method
        boolean isFourCornerEnabled = applicationResource.getFourCornerModelEnabled();

        Assert.assertEquals(false, isFourCornerEnabled);
    }
}
