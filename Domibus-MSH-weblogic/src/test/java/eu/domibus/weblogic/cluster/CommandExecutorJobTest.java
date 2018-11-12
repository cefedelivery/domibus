package eu.domibus.weblogic.cluster;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.server.ServerInfoService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu, Catalin Enache
 * @since 4.0.1
 */
@RunWith(JMockit.class)
public class CommandExecutorJobTest {


    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    private CommandService commandService;

    @Injectable
    private ServerInfoService serverInfoService;

    @Tested
    CommandExecutorJob commandExecutorJob;


    @Test
    public void testExecuteJob(@Injectable JobExecutionContext jobExecutionContext, @Injectable Command command) {
        final List<Command> commands = Arrays.asList(command);
        final Map<String, String> commandProperties = new HashMap<>();

        new Expectations() {{
            serverInfoService.getUniqueServerName();
            result = "msl";

            commandService.findCommandsByServerName(anyString);
            result = commands;

            command.getCommandName();
            result = Command.RELOAD_PMODE;

            command.getCommandProperties();
            result = commandProperties;
        }};

        Domain domain = DomainService.DEFAULT_DOMAIN;
        commandExecutorJob.executeJob(jobExecutionContext, domain);

        new FullVerifications() {{
            commandService.executeCommand(Command.RELOAD_PMODE, domain, commandProperties);

            commandService.deleteCommand(command.getEntityId());
        }};
    }
}
