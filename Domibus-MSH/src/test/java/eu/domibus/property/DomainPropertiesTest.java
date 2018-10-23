package eu.domibus.property;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class DomainPropertiesTest {

    public static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomainPropertiesTest.class);

    @Test
    public void testDomainPropertiesStartWith_domain_name() throws IOException {
        Properties properties = new Properties();
        ClassPathResource domainPropertiesResource = new ClassPathResource("domain_name-domibus.properties");
        String domainNamePropertiesString = IOUtils.toString(domainPropertiesResource.getInputStream(), Charset.forName("UTF-8"));
        //uncomment properties in order to be loaded by Properties.load
        domainNamePropertiesString = StringUtils.replace(domainNamePropertiesString, "#domain_name.", "domain_name.");
        domainNamePropertiesString = StringUtils.replace(domainNamePropertiesString, "#domibus.", "domibus.");
        LOGGER.info("Properties after replacement: {}", domainNamePropertiesString);
        properties.load(new ByteArrayInputStream(domainNamePropertiesString.getBytes()));
        final List<String> propertiesNotStartingWith_domain_name = properties.keySet().stream().map(property -> (String) property).filter(property -> !property.startsWith("domain_name")).collect(Collectors.toList());
        Assert.assertTrue(propertiesNotStartingWith_domain_name.isEmpty());

    }
}
