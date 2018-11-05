package eu.domibus.weblogic.cluster;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandProperty;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.cluster.CommandSkipService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.quartz.DomibusQuartzJobBean;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@DisallowConcurrentExecution //Only one worker runs at any time
public class CommandExecutorJob extends DomibusQuartzJobBean implements CommandSkipService {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(CommandExecutorJob.class);

    private static final String WEBLOGIC_NAME = "weblogic.Name";

    @Autowired
    private CommandService commandService;

    @Override
    protected void executeJob(JobExecutionContext context, Domain domain) {
        LOGGER.debug("Executing job...");

        String serverName = System.getProperty(WEBLOGIC_NAME);
        final List<Command> commandsByServerName = commandService.findCommandsByServerName(serverName);
        if (commandsByServerName == null) {
            return;
        }
        for (Command command : commandsByServerName) {
            final Map<String, String> commandProperties = command.getCommandProperties();
            if (!skipCommandSameServer(command.getCommandName(), domain, commandProperties)) {
                commandService.executeCommand(command.getCommandName(), domain, commandProperties);
            }
            commandService.deleteCommand(command.getEntityId());
        }
    }

    /**
     * Returns true if the commands is send to same server
     * @param command
     * @param domain
     * @param commandProperties
     * @return
     */
    @Override
    public boolean skipCommandSameServer(final String command, final Domain domain, Map<String, String> commandProperties) {
        String originServerName = commandProperties.get(CommandProperty.ORIGIN_SERVER);

        //execute the command
        if (StringUtils.isBlank(originServerName)) {
            return false;
        }
        final String serverName = System.getProperty(WEBLOGIC_NAME);

        if (serverName.equalsIgnoreCase(originServerName)) {
            LOGGER.info("Command [{}] for domain [{}] not executed as origin and actual server signature is the same [{}]", command, domain, serverName);
            return true;
        }
        return false;
    }
}
