package eu.domibus.jms.weblogic;

import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.jms.spi.helper.JMSSelectorUtil;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsOperations;

/**
 * Created by Cosmin Baciu on 30-Sep-16.
 */
@RunWith(JMockit.class)
public class JMSManagerWeblogicTest {

    @Tested
    JMSManagerWeblogic jmsManagerWeblogic;

    @Injectable
    JMXHelper jmxHelper;

    @Injectable
    private JmsOperations jmsSender;

    @Injectable
    JMSDestinationHelper jmsDestinationHelper;

    @Injectable
    JMSSelectorUtil jmsSelectorUtil;

    @Test
    public void testGetQueueName() throws Exception {
        String queueName = jmsManagerWeblogic.getQueueName("JmsModule!DomibusNotifyBackendEtrustexQueue");
        Assert.assertEquals(queueName, "DomibusNotifyBackendEtrustexQueue");

        queueName = jmsManagerWeblogic.getQueueName("DomibusNotifyBackendEtrustexQueue");
        Assert.assertEquals(queueName, "DomibusNotifyBackendEtrustexQueue");



    }
}
