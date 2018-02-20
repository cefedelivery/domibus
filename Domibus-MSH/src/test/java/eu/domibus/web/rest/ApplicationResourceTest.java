package eu.domibus.web.rest;

import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.web.rest.ro.DomibusInfoRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

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

    @Injectable("domibusProperties")
    Properties domibusProperties;

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
            domibusProperties.getProperty(ApplicationResource.DOMIBUS_CUSTOM_NAME, ApplicationResource.DOMIBUS_DEFAULTVALUE_NAME);
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
}
