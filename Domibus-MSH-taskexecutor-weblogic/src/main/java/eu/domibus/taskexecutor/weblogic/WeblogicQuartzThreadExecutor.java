package eu.domibus.taskexecutor.weblogic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.spi.ThreadExecutor;

public class WeblogicQuartzThreadExecutor implements ThreadExecutor {

    private static final Log LOG = LogFactory.getLog(WeblogicQuartzThreadExecutor.class);

    public void execute(Thread thread) {
        LOG.debug("Executing Quartz thread " + thread);
        DomibusWorkManagerTaskExecutor taskExecutor = SpringContextProvider.getApplicationContext().getBean("taskExecutor", DomibusWorkManagerTaskExecutor.class);
        taskExecutor.execute(thread);
    }

    public void initialize() {
        //nothing to initialize
    }


}

