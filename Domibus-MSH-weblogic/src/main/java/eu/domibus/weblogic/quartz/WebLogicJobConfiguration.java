package eu.domibus.weblogic.quartz;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.weblogic.cluster.CommandExecutorJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@Configuration("webLogicJobConfiguration")
public class WebLogicJobConfiguration {

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Bean
    public JobDetailFactoryBean commandExecutorJob() {
        JobDetailFactoryBean obj = new JobDetailFactoryBean();
        obj.setJobClass(CommandExecutorJob.class);
        obj.setDurability(true);
        return obj;
    }


    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public CronTriggerFactoryBean commandExecutorTrigger() {
        if (domainContextProvider.getCurrentDomainSafely() == null) {
            return null;
        }

        CronTriggerFactoryBean obj = new CronTriggerFactoryBean();
        obj.setJobDetail(commandExecutorJob().getObject());
        obj.setCronExpression(domibusPropertyProvider.getDomainProperty("domibus.cluster.command.cronExpression"));
        obj.setStartDelay(20000);
        return obj;
    }
}
