package eu.domibus.taskexecutor.wildfly;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.spi.ThreadExecutor;

public class WildFlyQuartzThreadExecutor implements ThreadExecutor {

    private static final Log LOG = LogFactory.getLog(WildFlyQuartzThreadExecutor.class);

    public void execute(Thread thread) {
        LOG.debug("Executing Quartz thread " + thread);
        DomibusWildFlyTaskExecutor taskExecutor = SpringContextProvider.getApplicationContext().getBean("taskExecutor", DomibusWildFlyTaskExecutor.class);
        taskExecutor.execute(thread);
    }

    public void initialize() {
        //nothing to initialize
    }


}

