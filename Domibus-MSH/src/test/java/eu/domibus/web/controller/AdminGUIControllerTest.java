package eu.domibus.web.controller;

import eu.domibus.common.MessageStatus;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.services.MessagesLogService;
import eu.domibus.common.util.DomibusPropertiesService;
import eu.domibus.ebms3.common.dao.PModeProvider;
import eu.domibus.plugin.NotificationListener;
import eu.domibus.plugin.routing.CriteriaFactory;
import eu.domibus.plugin.routing.RoutingService;
import eu.domibus.wss4j.common.crypto.TrustStoreService;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    private TrustStoreService trustStoreService;

    @Injectable
    private List<CriteriaFactory> routingCriteriaFactories;

    @Test
    public void testGetMessageStatuses() throws Exception {
        //temporarily revert the DOWNLOADED status to address the incompatibility issue EDELIVERY-2085
        final List<MessageStatus> messageStatuses = adminGUIController.getMessageStatuses();
        Assert.assertFalse(messageStatuses.contains(MessageStatus.DOWNLOADED));
    }
}
