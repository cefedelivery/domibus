package eu.domibus.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.logging.LoggingService;
import eu.domibus.core.logging.LoggingServiceImpl;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
@Service
public class CommandServiceImpl implements CommandService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CommandServiceImpl.class);

    @Autowired
    protected CommandDao commandDao;

    @Autowired
    private DomainCoreConverter domainConverter;

    @Autowired
    private PModeProvider pModeProvider;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    protected MultiDomainCryptoService multiDomainCryptoService;

    @Autowired
    protected LoggingService loggingService;

    @Override
    public void createClusterCommand(String command, String domain, String server, Map<String, Object> commandProperties) {
        LOG.debug("Creating command [{}] for domain [{}] and server [{}]", command, domain, server);
        CommandEntity commandEntity = new CommandEntity();
        commandEntity.setCommandName(command);
        commandEntity.setDomain(domain);
        commandEntity.setServerName(server);
        commandEntity.setCreationTime(new Date());
        commandEntity.setCommandProperties(getCommandProperties(commandProperties));
        commandDao.create(commandEntity);
    }

    @Override
    public List<Command> findCommandsByServerName(String serverName) {
        final List<CommandEntity> commands = commandDao.findCommandsByServerName(serverName);
        return domainConverter.convert(commands, Command.class);
    }

    @Override
    public void executeCommand(String command, Domain domain, Map<String, String> commandProperties) {
        LOG.debug("Executing command [{}] for domain [{}] having properties [{}]", command, domain, commandProperties);
        switch (command) {
            case Command.RELOAD_PMODE:
                pModeProvider.refresh();
                multiDomainCryptoService.refreshTrustStore(domain);
                break;
            case Command.EVICT_CACHES:
                Collection<String> cacheNames = cacheManager.getCacheNames();
                for (String cacheName : cacheNames) {
                    cacheManager.getCache(cacheName).clear();
                }
                break;
            case Command.RELOAD_TRUSTSTORE:
                multiDomainCryptoService.refreshTrustStore(domain);
                break;
            case Command.LOGGING_RESET:
                loggingService.resetLogging();
                break;
            case Command.LOGGING_SET_LEVEL:
                final String level = commandProperties.get(LoggingServiceImpl.COMMAND_LOG_LEVEL);
                final String name = commandProperties.get(LoggingServiceImpl.COMMAND_LOG_NAME);
                loggingService.setLoggingLevel(name, level);
                break;
            default:
                LOG.error("Unknown command received: " + command);
        }
    }

    @Override
    public void deleteCommand(Integer commandId) {
        final CommandEntity commandEntity = commandDao.read(commandId);
        if (commandEntity == null) {
            return;
        }
        commandDao.delete(commandEntity);
    }

    /**
     * just extract all message properties (of type {@code String})
     * excepting Command and Domain
     *
     * @param messageProperties
     * @return
     */
    protected Map<String, String> getCommandProperties(Map<String, Object> messageProperties) {
        HashMap<String, String> properties = new HashMap<>();

        if (MapUtils.isNotEmpty(messageProperties)) {
            for (Map.Entry<String, Object> entry : messageProperties.entrySet()) {
                if (!Command.COMMAND.equalsIgnoreCase(entry.getKey()) && !MessageConstants.DOMAIN.equalsIgnoreCase(entry.getKey())
                        && messageProperties.get(entry.getKey()) instanceof String) {
                    properties.put(entry.getKey(), (String) messageProperties.get(entry.getKey()));
                }
            }
        }
        return properties;
    }
}
