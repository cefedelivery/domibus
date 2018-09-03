package eu.domibus.common.services.impl;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.core.csv.MessageFilterCsvServiceImpl;
import eu.domibus.web.rest.ro.MessageFilterRO;
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
 * @since 4.0
 */

@RunWith(JMockit.class)
public class MessageFilterCsvServiceImplTest {

    private static final String MESSAGE_FILTER_HEADER = "Plugin,From,To,Action,Service,Persisted";

    private static final String LINE_SEPARATOR = "\n";

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Tested
    private MessageFilterCsvServiceImpl messageFilterCsvService;

    @Test
    public void testExportToCsv_EmptyList() throws CsvException {
        // Given
        // When
        final String exportToCSV = messageFilterCsvService.exportToCSV(new ArrayList<>(), null, null, null);

        // Then
        Assert.assertEquals(MESSAGE_FILTER_HEADER + LINE_SEPARATOR, exportToCSV);
    }

    @Test
    public void testExportToCsv_NullList() throws CsvException {
        // Given
        // When
        final String exportToCSV = messageFilterCsvService.exportToCSV(null, null, null, null);

        // Then
        Assert.assertEquals(MESSAGE_FILTER_HEADER + LINE_SEPARATOR, exportToCSV);
    }

    @Test
    public void testExportToCsv() throws CsvException {
        // Given
        List<MessageFilterRO> messageFilterROList = new ArrayList<>();
        MessageFilterRO messageFilterRO = new MessageFilterRO();
        messageFilterRO.setEntityId(1);
        messageFilterRO.setBackendName("backendName");
        messageFilterRO.setIndex(1);
        List<RoutingCriteria> routingCriterias = new ArrayList<>();
        RoutingCriteria fromRoutingCriteria = new RoutingCriteria();
        fromRoutingCriteria.setName("from");
        fromRoutingCriteria.setExpression("from:from");
        fromRoutingCriteria.setEntityId(1);
        routingCriterias.add(fromRoutingCriteria);
        messageFilterRO.setRoutingCriterias(routingCriterias);
        messageFilterRO.setPersisted(true);
        messageFilterROList.add(messageFilterRO);

        // When
        final String exportToCSV = messageFilterCsvService.exportToCSV(messageFilterROList, MessageFilterRO.class, null, null);

        // Then
        Assert.assertEquals(MESSAGE_FILTER_HEADER + LINE_SEPARATOR +
                "backendName,from:from,,,,true" + LINE_SEPARATOR, exportToCSV);
    }
}
