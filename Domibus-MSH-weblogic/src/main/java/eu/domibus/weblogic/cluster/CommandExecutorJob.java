package eu.domibus.weblogic.cluster;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu, Catalin Enache
 * @since 4.0.1
 */
@DisallowConcurrentExecution //Only one worker runs at any time
public class CommandExecutorJob extends DomibusQuartzJobBean {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(CommandExecutorJob.class);

    @Autowired
    private CommandService commandService;

    @Autowired
    private ServerInfoService serverInfoService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) {
        LOGGER.debug("Executing job...");

        String serverName = serverInfoService.getUniqueServerName();
        final List<Command> commandsByServerName = commandService.findCommandsByServerName(serverName);
        if (commandsByServerName == null) {
            return;
        }
        for (Command command : commandsByServerName) {
            final Map<String, String> commandProperties = command.getCommandProperties();
            commandService.executeCommand(command.getCommandName(), domain, commandProperties);
            commandService.deleteCommand(command.getEntityId());
        }
    }

}
