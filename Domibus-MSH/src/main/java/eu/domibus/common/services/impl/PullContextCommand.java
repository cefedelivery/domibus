package eu.domibus.common.services.impl;

import java.util.Map;

/**
 * Created by dussath on 6/6/17.
 *
 */
public interface PullContextCommand {
    void execute(Map<String, String> message);
}
