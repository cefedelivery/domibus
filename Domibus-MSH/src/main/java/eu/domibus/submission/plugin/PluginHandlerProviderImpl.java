package eu.domibus.submission.plugin;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.transformer.PluginHandler;
import eu.domibus.plugin.validation.SubmissionValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
@Component
public class PluginHandlerProviderImpl implements PluginHandlerProvider, ApplicationContextAware {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginHandlerProviderImpl.class);

    protected ApplicationContext applicationContext;

    @Override
    public PluginHandler getPluginHandler(String backendName) {
        List<String> matchedBeans = getMatches(backendName);
        if (matchedBeans == null || matchedBeans.isEmpty()) {
            LOG.debug("No submission validator bean configured for backend [" + backendName + "]");
            return null;
        }
        if (matchedBeans.size() > 1) {
            throw new SubmissionValidationException("There are multiple beans of type " + PluginHandler.class + " configured for backend [" + backendName + "]. Only one is allowed.");
        }

        String beanDefinitionName = matchedBeans.iterator().next();
        LOG.debug("Found submission validator bean [" + beanDefinitionName + "] for backend [" + backendName + "]");
        return applicationContext.getBean(beanDefinitionName, PluginHandler.class);
    }

    protected List<String> getMatches(String backendName) {
        List<String> result = new ArrayList<>();
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(PluginHandler.class);
        String backendNameLowerCase = StringUtils.lowerCase(backendName);
        for (String beanDefinitionName : beanDefinitionNames) {
            if (StringUtils.lowerCase(beanDefinitionName).contains(backendNameLowerCase)) {
                LOG.debug("Matched submission validator bean [" + beanDefinitionName + "] for backend [" + backendName + "]");
                result.add(beanDefinitionName);
            }
        }
        return result;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
