package eu.domibus.ebms3.puller;

import eu.domibus.common.services.MessageExchangeService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Created by dussath on 5/29/17.
 */
@DisallowConcurrentExecution //Only one SenderWorker runs at any time
public class MessagePullerJob extends QuartzJobBean {
    @Autowired
    private MessageExchangeService messagePullerService;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        messagePullerService.initiatePullRequest();
    }
}
