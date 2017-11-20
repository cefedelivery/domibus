package eu.domibus.tomcat.activemq;

import com.atomikos.icatch.TransactionServicePlugin;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component("domibusUserTransactionServiceImpConfigurationInit")
public class DomibusUserTransactionServiceImpConfigurationInit implements TransactionServicePlugin {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusUserTransactionServiceImpConfigurationInit.class);

    protected static final String OUTPUT_DIR = "com.atomikos.icatch.output_dir";

    @Override
    public void beforeInit(Properties properties) {
        createAtomikosOutputDirectory(properties);
    }

    protected void createAtomikosOutputDirectory(Properties properties) {
        final String outputDirectory = properties.getProperty(OUTPUT_DIR);
        LOGGER.debug("Creating directory [{}]", outputDirectory);

        if (StringUtils.isEmpty(outputDirectory)) {
            LOGGER.warn("The property [{}] is not defined", OUTPUT_DIR);
            return;
        }
        try {
            FileUtils.forceMkdir(new File(outputDirectory));
        } catch (IOException e) {
            LOGGER.error("Could not create directory [{}]", outputDirectory, e);
        }
    }

    @Override
    public void afterInit() {
        LOGGER.debug("afterInit");
    }

    @Override
    public void afterShutdown() {
        LOGGER.debug("afterShutdown");
    }
}
