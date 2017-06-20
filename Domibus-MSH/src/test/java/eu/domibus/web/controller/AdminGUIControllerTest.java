package eu.domibus.web.controller;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.services.MessagesLogService;
import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.routing.CriteriaFactory;
import eu.domibus.plugin.routing.RoutingService;
import eu.domibus.wss4j.common.crypto.CryptoService;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cache.CacheManager;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 3.2.4
 */
@RunWith(JMockit.class)
public class AdminGUIControllerTest {

    @Tested
    AdminGUIController adminGUIController;

    @Injectable
    private MessagesLogService messagesLogService;

    @Injectable
    private CacheManager cacheManager;

    @Injectable
    private ErrorLogDao eld; //TODO refactor, eliminate this.

    @Injectable
    private PModeProvider pModeProvider;

    @Injectable
    private DomibusPropertiesService domibusPropertiesService;

    @Injectable
    private RoutingService routingService;

    @Injectable
    private List<NotificationListener> notificationListenerServices;

    @Injectable
    private List<CriteriaFactory> routingCriteriaFactories;

    @Injectable
    CryptoService cryptoService;

    @Test
    public void testGetMessageStatuses() throws Exception {
        final List<MessageStatus> messageStatuses = adminGUIController.getMessageStatuses();
        Assert.assertTrue(messageStatuses.contains(MessageStatus.DOWNLOADED));
    }
}
