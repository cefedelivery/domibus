package eu.domibus.ebms3.puller;

import eu.domibus.common.services.MessageExchangeService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Thomas Dussart
 * @since 3.3
 */
@DisallowConcurrentExecution //Only one SenderWorker runs at any time
public class MessagePullerJob extends QuartzJobBean {
    @Autowired
    private MessageExchangeService messageExchangeService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //messageExchangeService.initiatePullRequest();
    }
}
