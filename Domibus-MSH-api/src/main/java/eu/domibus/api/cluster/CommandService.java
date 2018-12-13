package eu.domibus.api.cluster;

import eu.domibus.api.multitenancy.Domain;

import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
public interface CommandService {

    void createClusterCommand(String command, String domain, String server, Map<String, Object> commandProperties);

    List<Command> findCommandsByServerName(String serverName);

    List<Command> findCommandsByServerAndDomainName(String serverName, String domain);

    void executeCommand(String command, Domain domain, Map<String, String> commandProperties);

    void deleteCommand(Integer commandId);
}
