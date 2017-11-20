package eu.domibus.tomcat.activemq;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@RunWith(JMockit.class)
public class DomibusUserTransactionServiceImpConfigurationInitTest {


    @Tested
    DomibusUserTransactionServiceImpConfigurationInit domibusUserTransactionServiceImpConfigurationInit;

    @Test
    public void testBeforeInit(@Injectable Properties properties) throws Exception {
        new Expectations(domibusUserTransactionServiceImpConfigurationInit) {{
            domibusUserTransactionServiceImpConfigurationInit.createAtomikosOutputDirectory(properties);
        }};

        domibusUserTransactionServiceImpConfigurationInit.beforeInit(properties);
    }

    @Test
    public void testCreateAtomikosOutputDirectory(@Injectable Properties properties, @Mocked FileUtils fileUtils) throws Exception {
        new Expectations() {{
            properties.getProperty(DomibusUserTransactionServiceImpConfigurationInit.OUTPUT_DIR);
            result = "/home/outputDir";
        }};

        domibusUserTransactionServiceImpConfigurationInit.createAtomikosOutputDirectory(properties);

        new Verifications() {{
            fileUtils.forceMkdir(new File("/home/outputDir"));
            times = 1;
        }};
    }

    @Test
    public void testCreateAtomikosOutputDirectoryWhenPropertyIsNotDefined(@Injectable Properties properties, @Mocked FileUtils fileUtils) throws Exception {
        new Expectations() {{
            properties.getProperty(DomibusUserTransactionServiceImpConfigurationInit.OUTPUT_DIR);
            result = null;
        }};

        domibusUserTransactionServiceImpConfigurationInit.createAtomikosOutputDirectory(properties);

        new Verifications() {{
            fileUtils.forceMkdir(withAny(new File("")));
            times = 0;
        }};
    }

}