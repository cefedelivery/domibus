package eu.domibus.common.services.impl;

import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.web.rest.ro.MessageFilterRO;
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

    @Tested
    MessageFilterCsvServiceImpl messageFilterCsvService;

    @Test
    public void testExportToCsv_EmptyList() throws EbMS3Exception {
        // Given
        // When
        final String exportToCSV = messageFilterCsvService.exportToCSV(new ArrayList<>());

        // Then
        Assert.assertTrue(exportToCSV.isEmpty());
    }

    @Test
    public void testExportToCsv_NullList() throws EbMS3Exception {
        // Given
        // When
        final String exportToCSV = messageFilterCsvService.exportToCSV(null);

        // Then
        Assert.assertTrue(exportToCSV.isEmpty());
    }

    @Test
    public void testExportToCsv() throws EbMS3Exception {
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
        messageFilterRO.setIsPersisted(true);
        messageFilterROList.add(messageFilterRO);

        // When
        final String exportToCSV = messageFilterCsvService.exportToCSV(messageFilterROList);

        // Then
        Assert.assertEquals("Backend Name, From, To, Action, Service, Persisted" + System.lineSeparator() +
                "backendName,from:from, , , ,true" + System.lineSeparator(), exportToCSV);
    }
}
