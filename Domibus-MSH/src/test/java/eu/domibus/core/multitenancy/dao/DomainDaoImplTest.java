package eu.domibus.core.multitenancy.dao;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JMockit.class)
public class DomainDaoImplTest {

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Tested
    DomainDaoImpl domainDao;

    @Test
    public void findAll() {
        File f1 = new File("Zdomain-domibus.properties");
        File f2 = new File("Adomain-domibus.properties");

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;
            domibusConfigurationService.getConfigLocation();
            result = ".";
            domibusPropertyProvider.getProperty((Domain)any, anyString, "Adomain");
            result = "AAAdomain";
            domibusPropertyProvider.getProperty((Domain)any, anyString, "Zdomain");
            result = "ZZZdomain";
        }};
        new Expectations(FileUtils.class) {{
            FileUtils.listFiles((File) any, (String[]) any, false);
            result = Arrays.asList(f1, f2);
        }};

        List<Domain> domains = domainDao.findAll();

        assertEquals(3, domains.size());
        assertEquals("default", domains.get(0).getCode());
        assertEquals("Adomain", domains.get(1).getCode());
        assertEquals("Zdomain", domains.get(2).getCode());
    }

}