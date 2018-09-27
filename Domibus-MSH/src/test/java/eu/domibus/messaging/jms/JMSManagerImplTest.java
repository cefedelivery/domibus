package eu.domibus.messaging.jms;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.services.AuditService;
import eu.domibus.jms.spi.InternalJMSDestination;
import eu.domibus.jms.spi.InternalJMSManager;
import eu.domibus.jms.spi.InternalJmsMessage;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.Queue;
import java.util.*;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@RunWith(JMockit.class)
public class JMSManagerImplTest {

    @Tested
    JMSManagerImpl jmsManager;

    @Injectable
    InternalJMSManager internalJmsManager;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Injectable
    JMSDestinationMapper jmsDestinationMapper;

    @Injectable
    JMSMessageMapper jmsMessageMapper;

    @Injectable
    AuditService auditService;

    @Injectable
    DomibusConfigurationService domibusConfigurationService;

    @Injectable
    AuthUtils authUtils;

    @Injectable
    private MessageConverter messageConverter;

    @Injectable
    private JmsTemplate jsonJmsTemplate;

    @Injectable
    private DomainService domainService;

    @Test
    public void testGetDestinations() throws Exception {

        final Map<String, InternalJMSDestination> destinations = new HashMap<>();

        new Expectations() {{
            internalJmsManager.findDestinationsGroupedByFQName();
            result = destinations;
        }};

        jmsManager.getDestinations();

        new Verifications() {{
            jmsDestinationMapper.convert(destinations);
            times = 1;
        }};
    }

    @Test
    public void testGetMessage() throws Exception {
        final String source = "source";
        final String messageId = "messageId";
        final InternalJmsMessage internalJmsMessage = new InternalJmsMessage();

        new Expectations() {{
            internalJmsManager.getMessage(source, messageId);
            result = internalJmsMessage;
        }};

        jmsManager.getMessage(source, messageId);

        new Verifications() {{
            jmsMessageMapper.convert(internalJmsMessage);
            times = 1;
        }};
    }

    @Test
    public void testBrowseMessages() throws Exception {
        final String source = "source";
        final String jmsType = "jmsType";
        final Date fromDate = new Date();
        final Date toDate = new Date();
        final String selector = "myselector";
        final List<InternalJmsMessage> internalJmsMessage = new ArrayList<>();

        new Expectations() {{
            internalJmsManager.browseMessages(source, jmsType, fromDate, toDate, (String)any);
            result = internalJmsMessage;
        }};

        jmsManager.browseMessages(source, jmsType, fromDate, toDate, selector);

        new Verifications() {{
            jmsMessageMapper.convert(internalJmsMessage);
        }};
    }

    @Test
    public void testSendMessageToQueue() throws Exception {
        final JmsMessage message = new JmsMessage();
        final InternalJmsMessage messageSPI = new InternalJmsMessage();

        new Expectations() {{
            jmsMessageMapper.convert(message);
            result = messageSPI;
        }};

        jmsManager.sendMessageToQueue(message, "myqueue");

        new Verifications() {{
            jmsMessageMapper.convert(message);
            times = 1;

            internalJmsManager.sendMessage(messageSPI, "myqueue");

            Assert.assertEquals(message.getProperty(JmsMessage.PROPERTY_ORIGINAL_QUEUE), "myqueue");
        }};
    }

    @Test
    public void testSendMessageToJmsQueue(@Injectable final Queue queue) throws Exception {
        final JmsMessage message = new JmsMessage();
        final InternalJmsMessage messageSPI = new InternalJmsMessage();


        new Expectations() {{
            jmsMessageMapper.convert(message);
            result = messageSPI;

            queue.getQueueName();
            result = "myqueue";
        }};

        jmsManager.sendMessageToQueue(message, queue);

        new Verifications() {{
            jmsMessageMapper.convert(message);
            times = 1;

            internalJmsManager.sendMessage(messageSPI, queue);

            Assert.assertEquals(message.getProperty(JmsMessage.PROPERTY_ORIGINAL_QUEUE), "myqueue");
        }};
    }

    @Test
    public void testDeleteMessages() throws Exception {
        final String source = "myqueue";
        final String[] messageIds = new String[] {"1", "2"};

        jmsManager.deleteMessages(source, messageIds);

        new Verifications() {{
            internalJmsManager.deleteMessages(source, messageIds);
            auditService.addJmsMessageDeletedAudit("1", source);
            times = 1;
            auditService.addJmsMessageDeletedAudit("2", source);
            times = 1;
        }};
    }

    @Test
    public void testMoveMessages(@Injectable final Queue queue) throws Exception {
        final String source = "myqueue";
        final String destination = "destinationQueue";
        final String[] messageIds = new String[] {"1", "2"};

        jmsManager.moveMessages(source, destination, messageIds);

        new Verifications() {{
            internalJmsManager.moveMessages(source, destination, messageIds);
            auditService.addJmsMessageMovedAudit("1", source, destination);
            times = 1;
            auditService.addJmsMessageMovedAudit("2", source, destination);
            times = 1;
        }};
    }

    @Test
    public void testGetDomainSelector_MultiTenant_SuperAdmin() {

        final String selector = "myselector";

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            authUtils.isSuperAdmin();
            result = true;

        }};

        Assert.assertEquals(selector, jmsManager.getDomainSelector(selector));

        new FullVerifications(){{

        }};
    }

    @Test
    public void testGetDomainSelector_MultiTenant_Admin() {

        final String selector = "myselector";

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            authUtils.isSuperAdmin();
            result = false;

            domainContextProvider.getCurrentDomain();
            result = new Domain("digit", "digit");

        }};

        Assert.assertEquals(selector + " AND DOMAIN ='digit'", jmsManager.getDomainSelector(selector));

        new FullVerifications(){{}};
    }

    @Test
    public void testGetDomainSelector_MultiTenant_Admin_EmptySelector() {
        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            authUtils.isSuperAdmin();
            result = false;

            domainContextProvider.getCurrentDomain();
            result = new Domain("digit1", "digit1");

        }};

        Assert.assertEquals("DOMAIN ='digit1'", jmsManager.getDomainSelector(null));

        new FullVerifications(){{}};
    }

    @Test
    public void testJmsQueueInOtherDomain_Domain1Current_QueueDomain2() {
        final String jmsQueueInternalName = "domain2.domibus.backend.jms.outQueue";

        final List<Domain> domains = new ArrayList<>();
        domains.add(DomainService.DEFAULT_DOMAIN);
        Domain domain1 = new Domain();
        domain1.setCode("domain1");
        domain1.setName("Domain1");
        domains.add(domain1);

        Domain domain2 = new Domain();
        domain2.setCode("domain2");
        domain2.setName("Domain2");
        domains.add(domain2);

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            authUtils.isSuperAdmin();
            result = false;

            domainService.getDomains();
            result = domains;

            domainContextProvider.getCurrentDomainSafely();
            result = domain1;

        }};

        Assert.assertTrue(jmsManager.jmsQueueInOtherDomain(jmsQueueInternalName));

    }

    @Test
    public void testJmsQueueInOtherDomain_Domain1Current_QueueDomain1() {
        final String jmsQueueInternalName = "domain1.domibus.backend.jms.outQueue";

        final List<Domain> domains = new ArrayList<>();
        domains.add(DomainService.DEFAULT_DOMAIN);
        Domain domain1 = new Domain();
        domain1.setCode("domain1");
        domain1.setName("Domain1");
        domains.add(domain1);

        Domain domain2 = new Domain();
        domain2.setCode("domain2");
        domain2.setName("Domain2");
        domains.add(domain2);

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            authUtils.isSuperAdmin();
            result = false;

            domainService.getDomains();
            result = domains;

            domainContextProvider.getCurrentDomainSafely();
            result = domain1;

        }};

        Assert.assertFalse(jmsManager.jmsQueueInOtherDomain(jmsQueueInternalName));
    }

    @Test
    public void testJmsQueueInOtherDomain_NonMultitenancy() {
        final String jmsQueueInternalName = "domain1.domibus.backend.jms.outQueue";

        final List<Domain> domains = new ArrayList<>();
        domains.add(DomainService.DEFAULT_DOMAIN);


        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;
        }};

        Assert.assertFalse(jmsManager.jmsQueueInOtherDomain(jmsQueueInternalName));
    }

}
