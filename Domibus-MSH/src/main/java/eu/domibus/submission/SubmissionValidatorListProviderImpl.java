package eu.domibus.submission;

import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.plugin.validation.SubmissionValidatorList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class SubmissionValidatorListProviderImpl implements SubmissionValidatorListProvider, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(SubmissionValidatorListProviderImpl.class);

    protected ApplicationContext applicationContext;

    @Override
    public SubmissionValidatorList getSubmissionValidatorList(String backendName) {
        List<String> matchedBeans = getMatches(backendName);
        if(matchedBeans == null || matchedBeans.isEmpty()) {
            LOG.debug("No submission validator bean configured for backend [" + backendName + "]");
            return null;
        }
        if(matchedBeans.size() > 1) {
            throw new SubmissionValidationException("There are multiple beans of type " + SubmissionValidatorList.class + " configured for backend [" + backendName + "]. Only one is allowed.");
        }

        String beanDefinitionName = matchedBeans.iterator().next();
        LOG.debug("Found submission validator bean [" + beanDefinitionName + "] for backend [" + backendName + "]");
        return applicationContext.getBean(beanDefinitionName, SubmissionValidatorList.class);
    }

    protected List<String> getMatches(String backendName) {
        List<String> result = new ArrayList<>();
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(SubmissionValidatorList.class);
        String backendNameLowerCase = StringUtils.lowerCase(backendName);
        for (String beanDefinitionName : beanDefinitionNames) {
            if(StringUtils.lowerCase(beanDefinitionName).contains(backendNameLowerCase)) {
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
