package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.common.services.impl.MessageFilterCsvServiceImpl;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.plugin.routing.RoutingService;
import eu.domibus.web.rest.ro.MessageFilterRO;
import eu.domibus.web.rest.ro.MessageFilterResultRO;
import javafx.util.Pair;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageFilterResourceTest {

    private static final String CSV_TITLE = "Backend Name, From, To, Action, Service, Is Persisted";
    @Tested
    MessageFilterResource messageFilterResource;

    @Injectable
    RoutingService routingService;

    @Injectable
    DomainCoreConverter coreConverter;

    @Injectable
    MessageFilterCsvServiceImpl csvService;


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

    @Test
    public void testGetMessageFilterCsv() throws CsvException {
        // Given
        final String backendName = "Backend Filter 1";
        final String fromExpression = "from:expression";
        List<MessageFilterRO> messageFilterResultROS = new ArrayList<>();

        List<RoutingCriteria> routingCriterias = new ArrayList<>();
        RoutingCriteria routingCriteria = new RoutingCriteria();
        routingCriteria.setEntityId(1);
        routingCriteria.setName("From");
        routingCriteria.setExpression(fromExpression);
        routingCriterias.add(routingCriteria);

        MessageFilterRO messageFilterRO = new MessageFilterRO();
        messageFilterRO.setIndex(1);

        messageFilterRO.setBackendName(backendName);
        messageFilterRO.setEntityId(1);
        messageFilterRO.setRoutingCriterias(routingCriterias);
        messageFilterRO.setPersisted(true);

        messageFilterResultROS.add(messageFilterRO);

        new Expectations(messageFilterResource){{
            messageFilterResource.getBackendFiltersInformation();
            result = new Pair<>(messageFilterResultROS, true);
            csvService.exportToCSV(messageFilterResultROS);
            result = CSV_TITLE + backendName + "," + fromExpression + ", , , ," + true + System.lineSeparator();
        }};

        // When
        final ResponseEntity<String> csv = messageFilterResource.getCsv();

        // Then
        Assert.assertEquals(HttpStatus.OK, csv.getStatusCode());
        Assert.assertEquals(CSV_TITLE +
                        backendName + "," + fromExpression + ", , , ," + true + System.lineSeparator(),
                csv.getBody());
    }

    @Test
    public void testGetMessageFilterCsv_Exception() throws CsvException {
        // Given
        new Expectations() {{
            csvService.exportToCSV((List<?>) any);
            result = new CsvException(DomibusCoreErrorCode.DOM_001, "Exception", new Exception());
        }};

        // When
        final ResponseEntity<String> csv = messageFilterResource.getCsv();

        // Then
        Assert.assertEquals(HttpStatus.NO_CONTENT, csv.getStatusCode());
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
