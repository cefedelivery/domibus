package eu.domibus.jms.wildfly;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.server.ServerInfoService;
import eu.domibus.jms.spi.InternalJmsMessage;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.activemq.artemis.api.jms.management.JMSServerControl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;

import javax.jms.MapMessage;
import javax.management.MBeanServer;
import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
@RunWith(JMockit.class)
public class InternalJMSManagerWildFlyArtemisTest {

    @Tested
    InternalJMSManagerWildFlyArtemis jmsManager;

    @Injectable
    MBeanServer mBeanServer;

    @Injectable
    JMSServerControl jmsServerControl;

    @Injectable
    private JmsOperations jmsOperations;

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

    @Test
    public void testConvertMapMessage(final @Injectable MapMessage mapMessage) throws Exception {
        final String jmsType = "mytype";
        final Date jmsTimestamp = new Date();
        final String jmsId1 = "jmsId1";
        final List<String> allPropertyNames = Arrays.asList("JMSProp1", "totalNumberOfPayloads");
        final List<String> mapNames = Arrays.asList("payload_1");

        new NonStrictExpectations() {
            {
                mapMessage.getJMSType();
                result = jmsType;

                mapMessage.getJMSTimestamp();
                result = jmsTimestamp.getTime();

                mapMessage.getJMSMessageID();
                result = jmsId1;

                mapMessage.getPropertyNames();
                result = new Vector(allPropertyNames).elements();

                mapMessage.getObjectProperty("JMSProp1");
                result = "JMSValue1";

                mapMessage.getObjectProperty("totalNumberOfPayloads");
                result = 5;

                mapMessage.getMapNames();
                result = new Vector(mapNames).elements();

                mapMessage.getObject("payload_1");
                result = "payload";
            }
        };

        InternalJmsMessage internalJmsMessage = jmsManager.convert(mapMessage);

        assertEquals(internalJmsMessage.getType(), jmsType);
        assertEquals(internalJmsMessage.getTimestamp(), jmsTimestamp);
        assertEquals(internalJmsMessage.getId(), jmsId1);

        Map<String, Object> properties = internalJmsMessage.getProperties();
        assertEquals(properties.size(), 2);
        assertEquals(properties.get("JMSProp1"), "JMSValue1");
        assertEquals(properties.get("totalNumberOfPayloads"), 5);
        assertEquals(properties.get("payload_1"), null);
    }
}