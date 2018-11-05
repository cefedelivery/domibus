package eu.domibus.api.cluster;

import eu.domibus.api.multitenancy.Domain;

import java.util.Map;

/**
 * Checks if the command should be skipped on the same server or not
 *
 * @author Catalin Enache
 * @since 4.1
 */
public interface CommandSkipService {

    boolean skipCommandSameServer(final String command, final Domain domain, Map<String, String> commandProperties);
}
