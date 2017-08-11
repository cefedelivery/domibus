package eu.domibus.jms.weblogic;

import eu.domibus.jms.spi.InternalJMSException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.IOException;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Component
public class DefaultJMXTemplate implements JMXTemplate {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DefaultJMXTemplate.class);

    @Autowired
    JMXHelper jmxHelper;

    @Override
    public <T> T query(JMXOperation jmxOperation) {
        LOG.debug("Executing JMX operation");
        try (JMXConnector jmxConnector = jmxHelper.getJMXConnector();) {
            MBeanServerConnection mbsc = jmxHelper.getDomainRuntimeMBeanServerConnection(jmxConnector);
            return jmxOperation.execute(mbsc);
        } catch (IOException e) {
            throw new InternalJMSException("Error executing JMX query", e);
        }
    }
}
