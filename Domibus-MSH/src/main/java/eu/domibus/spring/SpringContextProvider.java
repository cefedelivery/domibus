package eu.domibus.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * @author Christian Koch, Stefan Mueller, Cosmin Baciu
 */
@Service
public class SpringContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @SuppressWarnings("squid:S2696") //TODO: SONAR version updated!
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
}