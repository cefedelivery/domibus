package eu.domibus.quartz;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.ebms3.common.quartz.AutowiringSpringBeanJobFactory;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Configuration
@DependsOn("springContextProvider")
public class DomainSchedulerFactoryConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomainSchedulerFactoryConfiguration.class);

    @Autowired
    @Qualifier("taskExecutor")
    protected Executor executor;

    @Autowired
    @Qualifier("quartzTaskExecutor")
    protected Executor quartzTaskExecutor;

    @Autowired
    protected ApplicationContext applicationContext;

    @Qualifier("domibusJDBC-XADataSource")
    @Autowired
    protected DataSource dataSource;

    @Qualifier("domibusJDBC-nonXADataSource")
    @Autowired
    protected DataSource nonTransactionalDataSource;

    @Autowired
    protected PlatformTransactionManager transactionManager;

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory;

    @Autowired
    protected DomainService domainService;

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public SchedulerFactoryBean schedulerFactory(Domain domain) {
        LOG.debug("Instantiating the scheduler factory for domain [{}]", domain);

        //get all the Spring Bean Triggers so that new instances with scope prototype are injected
        final Map<String, Trigger> beansOfType = applicationContext.getBeansOfType(Trigger.class);
        List<Trigger> domibusStandardTriggerList = new ArrayList<>(beansOfType.values());

        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();

        final String schedulerName = domainService.getSchedulerName(domain);
        scheduler.setSchedulerName(schedulerName);
        scheduler.setTaskExecutor(executor);
        scheduler.setAutoStartup(false);
        scheduler.setApplicationContext(applicationContext);
        scheduler.setWaitForJobsToCompleteOnShutdown(true);
        scheduler.setOverwriteExistingJobs(true);
        scheduler.setDataSource(dataSource);
        scheduler.setNonTransactionalDataSource(nonTransactionalDataSource);
        scheduler.setTransactionManager(transactionManager);
        Properties properties = new Properties();
        properties.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        properties.setProperty("org.quartz.jobStore.isClustered", domibusPropertyProvider.getProperty("domibus.deployment.clustered"));
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
        properties.setProperty("org.quartz.jobStore.useProperties", "false");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        properties.setProperty("org.quartz.scheduler.jmx.export", "true");
        properties.setProperty("org.quartz.threadExecutor.class", DomibusQuartzThreadExecutor.class.getCanonicalName());

        properties.setProperty("org.quartz.scheduler.instanceName", schedulerName);
        final String tablePrefix = getTablePrefix(domain);
        if (StringUtils.isNotEmpty(tablePrefix)) {
            LOG.debug("Using the Quartz tablePrefix [{}] for domain [{}]", tablePrefix, domain);
            properties.setProperty("org.quartz.jobStore.tablePrefix", tablePrefix);
        }

        scheduler.setQuartzProperties(properties);

        scheduler.setJobFactory(autowiringSpringBeanJobFactory);
        scheduler.setTriggers(domibusStandardTriggerList.toArray(new Trigger[0]));

        return scheduler;
    }


    protected String getTablePrefix(Domain domain) {
        final String databaseSchema = domainService.getDatabaseSchema(domain);
        if (StringUtils.isEmpty(databaseSchema)) {
            return null;
        }

        return databaseSchema + ".QRTZ_";
    }
}
