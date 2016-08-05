package eu.domibus.submission;

import eu.domibus.plugin.validation.SubmissionValidatorList;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
@Component
public class SubmissionValidatorListProviderImpl implements SubmissionValidatorListProvider, ApplicationContextAware {

    private static final Log LOG = LogFactory.getLog(SubmissionValidatorListProviderImpl.class);

    protected ApplicationContext applicationContext;

    @Override
    public SubmissionValidatorList getSubmissionValidatorList(String backendName) {
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(SubmissionValidatorList.class);
        String backendNameLowerCase = StringUtils.lowerCase(backendName);
        for (String beanDefinitionName : beanDefinitionNames) {
            if(StringUtils.lowerCase(beanDefinitionName).contains(backendNameLowerCase)) {
                LOG.debug("Found submission validator bean [" + beanDefinitionName + "] for backend [" + backendName + "]");
                return applicationContext.getBean(beanDefinitionName, SubmissionValidatorList.class);
            }
        }
        return null;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
