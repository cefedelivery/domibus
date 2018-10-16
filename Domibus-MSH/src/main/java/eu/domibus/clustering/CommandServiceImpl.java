package eu.domibus.clustering;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cluster.CommandService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.pmode.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

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

    @Override
    public void createClusterCommand(String command, String domain, String server) {
        LOG.debug("Creating command [{}] for domain [{}] and server [{}]", command, domain, server);
        CommandEntity commandEntity = new CommandEntity();
        commandEntity.setCommandName(command);
        commandEntity.setDomain(domain);
        commandEntity.setServerName(server);
        commandEntity.setCreationTime(new Date());
        commandDao.create(commandEntity);
    }

    @Override
    public List<Command> findCommandsByServerName(String serverName) {
        final List<CommandEntity> commands = commandDao.findCommandsByServerName(serverName);
        return domainConverter.convert(commands, Command.class);
    }

    @Override
    public void executeCommand(String command, Domain domain) {
        LOG.debug("Executing command [{}] for domain [{}]", command, domain);
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
}
