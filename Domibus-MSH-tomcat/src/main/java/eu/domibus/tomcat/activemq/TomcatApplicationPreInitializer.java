package eu.domibus.tomcat.activemq;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.property.PropertyResolver;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * This class executes before the beans from the Application Context are initialized
 */
@Component
public class TomcatApplicationPreInitializer implements BeanFactoryPostProcessor, PriorityOrdered {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(TomcatApplicationPreInitializer.class);

    protected static final String OUTPUT_DIR = "com.atomikos.icatch.output_dir";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final Properties domibusProperties = beanFactory.getBean("domibusProperties", Properties.class);
        createAtomikosOutputDirectory(domibusProperties);
    }

    protected void createAtomikosOutputDirectory(Properties domibusProperties) {
        PropertyResolver propertyResolver = new PropertyResolver();
        final String outputDirectory = propertyResolver.getResolvedProperty(OUTPUT_DIR, domibusProperties, true);
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
}  