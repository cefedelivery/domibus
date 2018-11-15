package eu.domibus.tomcat;

import eu.domibus.api.server.ServerInfoService;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;

/**
 * {@inheritDoc}
 */
@Service
public class ServerInfoServiceImpl implements ServerInfoService {

    @Override
    public String getUniqueServerName() {
        return ManagementFactory.getRuntimeMXBean().getName();
    }

    @Override
    public String getServerName() {
        return getUniqueServerName().split("@")[1];
    }
}
