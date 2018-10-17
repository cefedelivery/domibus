package eu.domibus.api.cluster;

import eu.domibus.api.multitenancy.Domain;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0.1
 */
public interface CommandService {

    void createClusterCommand(String command, String domain, String server);

    List<Command> findCommandsByServerName(String serverName);

    void executeCommand(String command, Domain domain);

    void deleteCommand(Integer commandId);
}
