package eu.domibus.weblogic.cluster;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;

import java.util.Arrays;
import java.util.List;

/**
 * @author Cosmin Baciu
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

    @Tested
    CommandExecutorJob commandExecutorJob;


    @Test
    public void testExecuteJob(@Injectable JobExecutionContext jobExecutionContext, @Injectable Command command) {
        final List<Command> commands = Arrays.asList(command);

        new MockUp<System>() {
            @Mock
            public String getProperty(String value) {
                return "ms1";
            }
        };

        new Expectations() {{
            command.getCommandName();
            result = Command.RELOAD_PMODE;

            commandService.findCommandsByServerName("ms1");
            result = commands;
        }};

        Domain domain = DomainService.DEFAULT_DOMAIN;
        commandExecutorJob.executeJob(jobExecutionContext, domain);

        new Verifications() {{
            commandService.executeCommand(Command.RELOAD_PMODE, domain);
        }};
    }
}
