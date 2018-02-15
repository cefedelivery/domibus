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

    @Test
    public void testGetDomibusName() {
        // Given
        new Expectations(applicationResource) {{
           domibusProperties.getProperty(applicationResource.DOMIBUS_CUSTOM_NAME, applicationResource.DOMIBUS_DEFAULTVALUE_NAME);
           result = DOMIBUS_CUSTOMIZED_NAME;
        }};

        // When
        final String domibusName = applicationResource.getDomibusName();

        // Then
        Assert.assertEquals(DOMIBUS_CUSTOMIZED_NAME, domibusName);


    }
}
