package eu.domibus.quartz;

import eu.domibus.spring.SpringContextProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.spi.ThreadExecutor;
import org.springframework.core.task.TaskExecutor;

public class DomibusQuartzThreadExecutor implements ThreadExecutor {

    private static final Log LOG = LogFactory.getLog(DomibusQuartzThreadExecutor.class);

    public void execute(Thread thread) {
        LOG.debug("Executing Quartz thread " + thread);
        TaskExecutor taskExecutor = SpringContextProvider.getApplicationContext().getBean("quartzTaskExecutor", TaskExecutor.class);
        taskExecutor.execute(thread);
    }

    public void initialize() {
        //nothing to initialize
    }


}

