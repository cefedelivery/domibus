package eu.domibus.web.rest;

import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.plugin.routing.RoutingService;
import eu.domibus.web.rest.ro.MessageFilterRO;
import eu.domibus.web.rest.ro.MessageFilterResultRO;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageFilterResourceTest {

    @Tested
    MessageFilterResource messageFilterResource;

    @Injectable
    RoutingService routingService;

    @Injectable
    DomainCoreConverter coreConverter;

    @Test
    public void testGetMessageFilterPersisted() {
        MessageFilterResultRO messageFilterResultRO = getMessageFilterResultRO(0);

        // Then
        Assert.assertNotNull(messageFilterResultRO);
        Assert.assertFalse(messageFilterResultRO.isAreFiltersPersisted());
        Assert.assertEquals(getMessageFilterROS(0), messageFilterResultRO.getMessageFilterEntries());
    }

    @Test
    public void testGetMessageFilterNotPersisted() {
        MessageFilterResultRO messageFilterResultRO = getMessageFilterResultRO(1);

        // Then
        Assert.assertNotNull(messageFilterResultRO);
        Assert.assertTrue(messageFilterResultRO.isAreFiltersPersisted());
        Assert.assertEquals(getMessageFilterROS(1), messageFilterResultRO.getMessageFilterEntries());
    }

    private MessageFilterResultRO getMessageFilterResultRO(int messageFilterEntityId) {
        // Given
        final ArrayList<BackendFilter> backendFilters = new ArrayList<>();
        BackendFilter backendFilter = new BackendFilter();
        backendFilter.setEntityId(1);
        backendFilter.setBackendName("backendName1");
        backendFilter.setIndex(0);
        backendFilter.setActive(true);
        backendFilters.add(backendFilter);

        final List<MessageFilterRO> messageFilterROS = getMessageFilterROS(messageFilterEntityId);

        new Expectations() {{
            routingService.getBackendFiltersUncached();
            result = backendFilters;

            coreConverter.convert(backendFilters, MessageFilterRO.class);
            result = messageFilterROS;
        }};

        // When
        return messageFilterResource.getMessageFilter();
    }

    private List<MessageFilterRO> getMessageFilterROS(int messageFilterEntityId) {
        final List<MessageFilterRO> messageFilterROS = new ArrayList<>();
        MessageFilterRO messageFilterRO = new MessageFilterRO();
        messageFilterRO.setEntityId(messageFilterEntityId);
        messageFilterRO.setPersisted(messageFilterEntityId != 0);
        messageFilterROS.add(messageFilterRO);
        return messageFilterROS;
    }
}
