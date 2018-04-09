package eu.domibus.quartz;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.spring.SpringContextProvider;
import org.quartz.spi.ThreadExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;

public class DomibusQuartzThreadExecutor implements ThreadExecutor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusQuartzThreadExecutor.class);

    @Autowired
    ApplicationContext applicationContext;

    public void execute(Thread thread) {
        LOG.debug("Executing Quartz thread " + thread);
        TaskExecutor taskExecutor = SpringContextProvider.getApplicationContext().getBean("quartzTaskExecutor", TaskExecutor.class);
        taskExecutor.execute(thread);
    }

    public void initialize() {
        //nothing to initialize
    }


}

