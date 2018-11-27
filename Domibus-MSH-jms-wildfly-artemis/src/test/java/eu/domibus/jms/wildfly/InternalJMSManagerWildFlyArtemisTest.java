package eu.domibus.jms.wildfly;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.artemis.api.jms.management.JMSServerControl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class InternalJMSManagerWildFlyArtemisTest {

    @Injectable
    MBeanServer mBeanServer;

    @Injectable
    JMSServerControl jmsServerControl;

    @Injectable
    private JmsOperations jmsSender;

    @Injectable
    JMSDestinationHelper jmsDestinationHelper;

    @Injectable
    JMSSelectorUtil jmsSelectorUtil;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private AuthUtils authUtils;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private ServerInfoService serverInfoService;

    @Tested
    InternalJMSManagerWildFlyArtemis internalJMSManagerWildFlyArtemis;

    @Test
    public void findDestinationsGroupedByFQName(final @Mocked ObjectName objectName) throws  Exception {
        final Map<String, ObjectName> queueMap = new HashMap<>();
        ObjectName queue1 = new ObjectName("backendWebserviceQueue");
        queueMap.put("queue1", objectName);

        new Expectations(internalJMSManagerWildFlyArtemis) {{
            internalJMSManagerWildFlyArtemis.getQueueMap();
            result = queueMap;
        }};

        internalJMSManagerWildFlyArtemis.findDestinationsGroupedByFQName();

        new FullVerifications() {{

        }};
    }

    @Test
    public void getQueueMap() {
    }

    @Test
    public void getTopicMap() {
    }

    @Test
    public void getQueueControl() {
    }

    @Test
    public void getTopicControl() {
    }

    @Test
    public void getQueueControl1() {
    }

    @Test
    public void getTopicControl1() {
    }

    @Test
    public void getQueue() {
    }

    @Test
    public void getTopic() {
    }

    @Test
    public void getJndiName() {
    }

    @Test
    public void getJndiName1() {
    }

    @Test
    public void lookupQueue() {
    }

    @Test
    public void lookupTopic() {
    }

    @Test
    public void sendMessage() {
    }

    @Test
    public void sendMessage1() {
    }

    @Test
    public void sendMessageToTopic() {
    }

    @Test
    public void sendMessageToTopic1() {
    }

    @Test
    public void deleteMessages() {
    }

    @Test
    public void getMessage() {
    }

    @Test
    public void browseClusterMessages() {
    }

    @Test
    public void browseMessages() {
    }

    @Test
    public void convert() {
    }

    @Test
    public void convert1() {
    }

    @Test
    public void moveMessages() {
    }

    @Test
    public void consumeMessage() {
    }
}