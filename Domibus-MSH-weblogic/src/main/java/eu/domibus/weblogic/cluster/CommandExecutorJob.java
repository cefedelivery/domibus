package eu.domibus.weblogic.cluster;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@Conditional(CommandExecutorCondition.class)
@DisallowConcurrentExecution //Only one worker runs at any time
public class CommandExecutorJob extends DomibusQuartzJobBean {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(CommandExecutorJob.class);

    @Autowired
    private CommandService commandService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) {
        LOGGER.debug("Executing job...");

        String serverName = System.getProperty("weblogic.Name");
        final List<Command> commandsByServerName = commandService.findCommandsByServerName(serverName);
        if (commandsByServerName == null) {
            return;
        }
        for (Command command : commandsByServerName) {
            commandService.executeCommand(command.getCommandName(), domain);
            commandService.deleteCommand(command.getEntityId());
        }
    }
}
