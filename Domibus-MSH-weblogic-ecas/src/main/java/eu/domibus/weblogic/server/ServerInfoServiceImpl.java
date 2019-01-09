package eu.domibus.weblogic.server;

import eu.domibus.api.server.ServerInfoService;
import org.springframework.stereotype.Service;

/**
 * {@inheritDoc}
 */
@Service
public class ServerInfoServiceImpl implements ServerInfoService {

    private static final String WEBLOGIC_NAME = "weblogic.Name";

    @Override
    public String getUniqueServerName() {
        return System.getProperty(WEBLOGIC_NAME);
    }

    @Override
    public String getServerName() {
        return getUniqueServerName();
    }
}
