package eu.domibus.quartz;

import eu.domibus.spring.SpringContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.spi.ThreadExecutor;
import org.springframework.core.task.TaskExecutor;

public class DomibusQuartzThreadExecutor implements ThreadExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(DomibusQuartzThreadExecutor.class);

    public void execute(Thread thread) {
        LOG.debug("Executing Quartz thread " + thread);
        TaskExecutor taskExecutor = SpringContextProvider.getApplicationContext().getBean("quartzTaskExecutor", TaskExecutor.class);
        taskExecutor.execute(thread);
    }

    public void initialize() {
        //nothing to initialize
    }


}

