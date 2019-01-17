package eu.domibus.submission;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.transformer.OutgoingMessageTransformerList;
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
public class OutgoingMessageTransformerListProviderImpl implements OutgoingMessageTransformerListProvider, ApplicationContextAware {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(OutgoingMessageTransformerListProviderImpl.class);

    protected ApplicationContext applicationContext;

    @Override
    public OutgoingMessageTransformerList getOutgoingMessageTransformerList(String backendName) {
        List<String> matchedBeans = getMatches(backendName);
        if (matchedBeans == null || matchedBeans.isEmpty()) {
            LOG.debug("No message transformer bean configured for backend [" + backendName + "]");
            return null;
        }
        if (matchedBeans.size() > 1) {
            throw new SubmissionValidationException("There are multiple beans of type " + OutgoingMessageTransformerList.class + " configured for backend [" + backendName + "]. Only one is allowed.");
        }

        String beanDefinitionName = matchedBeans.iterator().next();
        LOG.debug("Found message transformer bean [" + beanDefinitionName + "] for backend [" + backendName + "]");
        return applicationContext.getBean(beanDefinitionName, OutgoingMessageTransformerList.class);
    }

    protected List<String> getMatches(String backendName) {
        List<String> result = new ArrayList<>();
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(OutgoingMessageTransformerList.class);
        String backendNameLowerCase = StringUtils.lowerCase(backendName);
        for (String beanDefinitionName : beanDefinitionNames) {
            if (StringUtils.lowerCase(beanDefinitionName).contains(backendNameLowerCase)) {
                LOG.debug("Matched message transformer bean [" + beanDefinitionName + "] for backend [" + backendName + "]");
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
