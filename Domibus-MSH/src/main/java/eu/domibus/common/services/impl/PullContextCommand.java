package eu.domibus.common.services.impl;

import java.util.Map;

/**
 * @author Thomas Dussart
 * @since 3.3
 *
 */
public interface PullContextCommand {
    void execute(Map<String, String> message);
}
