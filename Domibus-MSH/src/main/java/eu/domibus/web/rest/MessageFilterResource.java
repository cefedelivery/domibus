package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.core.csv.CsvService;
import eu.domibus.core.csv.MessageFilterCsvServiceImpl;
import eu.domibus.plugin.routing.RoutingService;
import eu.domibus.web.rest.ro.MessageFilterRO;
import eu.domibus.web.rest.ro.MessageFilterResultRO;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/messagefilters")
public class MessageFilterResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageFilterResource.class);

    @Autowired
    RoutingService routingService;

    @Autowired
    DomainCoreConverter coreConverter;

    @Autowired
    private MessageFilterCsvServiceImpl messageFilterCsvServiceImpl;

    protected Pair<List<MessageFilterRO>,Boolean> getBackendFiltersInformation() {
        boolean areFiltersPersisted = true;
        List<BackendFilter> backendFilters = routingService.getBackendFiltersUncached();
        List<MessageFilterRO> messageFilterResultROS = coreConverter.convert(backendFilters, MessageFilterRO.class);
        for (MessageFilterRO messageFilter : messageFilterResultROS) {
            if(messageFilter.getEntityId() == 0) {
                messageFilter.setPersisted(false);
                areFiltersPersisted = false;
            } else {
                messageFilter.setPersisted(true);
            }
        }
        return new ImmutablePair<>(messageFilterResultROS,areFiltersPersisted);
    }

    @GetMapping
    public MessageFilterResultRO getMessageFilter() {
        final Pair<List<MessageFilterRO>, Boolean> backendFiltersInformation = getBackendFiltersInformation();

        MessageFilterResultRO resultRO = new MessageFilterResultRO();
        resultRO.setMessageFilterEntries(backendFiltersInformation.getKey());
        resultRO.setAreFiltersPersisted(backendFiltersInformation.getValue());
        return resultRO;
    }

    @PutMapping
    public void updateMessageFilters(@RequestBody List<MessageFilterRO> messageFilterROS) {
        List<BackendFilter> backendFilters = coreConverter.convert(messageFilterROS, BackendFilter.class);
        routingService.updateBackendFilters(backendFilters);
    }

    /**
     * This method returns a CSV file with the contents of Message Filter table
     *
     * @return CSV file with the contents of Message Filter table
     */
    @GetMapping(path = "/csv")
    public ResponseEntity<String> getCsv() {
        String resultText;
        try {
            resultText = messageFilterCsvServiceImpl.exportToCSV(getBackendFiltersInformation().getKey(),
                    MessageFilterRO.class, null, null);
        } catch (CsvException e) {
            LOGGER.error("Exception caught during export to CSV", e);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CsvService.APPLICATION_EXCEL_STR))
                .header("Content-Disposition", "attachment; filename=" + messageFilterCsvServiceImpl.getCsvFilename("message-filter"))
                .body(resultText);
    }
}
