package eu.domibus.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.logging.LoggingService;
import eu.domibus.core.logging.LoggingServiceImpl;
import eu.domibus.core.pmode.PModeProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.CacheManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@RunWith(JMockit.class)
public class CommandServiceImplTest {

    @Injectable
    protected CommandDao commandDao;

    @Injectable
    private DomainCoreConverter domainConverter;

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private CacheManager cacheManager;

    @Injectable
    protected MultiDomainCryptoService multiDomainCryptoService;

    @Injectable
    LoggingService loggingService;

    @Tested
    CommandServiceImpl commandService;

    @Test
    public void testCreateClusterCommand() {
        String command = "command1";
        String domain = "domain1";
        String server = "server1";

        commandService.createClusterCommand(command, domain, server, null);

        new Verifications() {{
            CommandEntity entity = null;
            commandDao.create(entity = withCapture());

            assertEquals(entity.getCommandName(), command);
            assertEquals(entity.getDomain(), domain);
            assertEquals(entity.getServerName(), server);
        }};
    }

    @Test
    public void testFindCommandsByServerName() {
        String server = "server1";

        commandService.findCommandsByServerName(server);

        new Verifications() {{
            commandDao.findCommandsByServerName(server);
        }};
    }

    @Test
    public void testExecuteReloadPModeCommand() {
        commandService.executeCommand(Command.RELOAD_PMODE, DomainService.DEFAULT_DOMAIN, null);

        new Verifications() {{
            pModeProvider.refresh();
            multiDomainCryptoService.refreshTrustStore(DomainService.DEFAULT_DOMAIN);
        }};
    }

    @Test
    public void testExecuteReloadTruststoreCommand() {
        commandService.executeCommand(Command.RELOAD_TRUSTSTORE, DomainService.DEFAULT_DOMAIN, null);

        new Verifications() {{
            multiDomainCryptoService.refreshTrustStore(DomainService.DEFAULT_DOMAIN);
        }};
    }

    @Test
    public void testExecuteEvictCacheCommand() {
        List<String> cacheList = Arrays.asList("cache1", "cache2");
        new Expectations() {{
            cacheManager.getCacheNames();
            result = cacheList;
        }};

        commandService.executeCommand(Command.EVICT_CACHES, DomainService.DEFAULT_DOMAIN, null);

        new Verifications() {{
            cacheManager.getCache("cache1").clear();
        }};
    }

    @Test
    public void testExecuteSetLoggingLevelCommand() {
        final Map<String, String> commandProperties = new HashMap<>();
        final String level = "DEBUG";
        final String name = "eu.domibus";
        commandProperties.put(LoggingServiceImpl.COMMAND_LOG_LEVEL, level);
        commandProperties.put(LoggingServiceImpl.COMMAND_LOG_NAME, name);
        commandService.executeCommand(Command.LOGGING_SET_LEVEL, DomainService.DEFAULT_DOMAIN, commandProperties);

        new Verifications() {{
            final String nameActual, levelActual;
            loggingService.setLoggingLevel(nameActual = withCapture(), levelActual = withCapture());
            Assert.assertEquals(level, levelActual);
            Assert.assertEquals(name, nameActual);
        }};
    }

    @Test
    public void testExecuteResetLoggingCommand() {

        commandService.executeCommand(Command.LOGGING_RESET, DomainService.DEFAULT_DOMAIN, null);

        new Verifications() {{
            loggingService.resetLogging();
        }};
    }
}
