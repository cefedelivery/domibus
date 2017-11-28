package eu.domibus.web.rest;

import eu.domibus.api.routing.BackendFilter;
import eu.domibus.core.converter.DomainCoreConverter;
import eu.domibus.plugin.routing.RoutingService;
import eu.domibus.web.rest.ro.MessageFilterRO;
import eu.domibus.web.rest.ro.MessageFilterResultRO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RestController
@RequestMapping(value = "/rest/messagefilters")
public class MessageFilterResource {

    @Autowired
    RoutingService routingService;

    @Autowired
    DomainCoreConverter coreConverter;

    private List<MessageFilterRO> getBackendFiltersInformation() {
        List<BackendFilter> backendFilters = routingService.getBackendFiltersUncached();
        List<MessageFilterRO> messageFilterResultROS = coreConverter.convert(backendFilters, MessageFilterRO.class);
        for (MessageFilterRO messageFilter : messageFilterResultROS) {
            if(messageFilter.getEntityId() == 0) {
                messageFilter.setPersisted(false);
            } else {
                messageFilter.setPersisted(true);
            }
        }
        return messageFilterResultROS;
    }

    @RequestMapping(method = RequestMethod.GET)
    public MessageFilterResultRO getMessageFilter() {
        List<BackendFilter> backendFilters = routingService.getBackendFiltersUncached();
        List<MessageFilterRO> messageFilterResultROS = coreConverter.convert(backendFilters, MessageFilterRO.class);
        boolean areFiltersPersisted = true;
        for (MessageFilterRO messageFilter : messageFilterResultROS) {
            if(messageFilter.getEntityId() == 0) {
                messageFilter.setPersisted(false);
                areFiltersPersisted = false;
            } else {
                messageFilter.setPersisted(true);
            }
        }

        MessageFilterResultRO resultRO = new MessageFilterResultRO();
        resultRO.setMessageFilterEntries(messageFilterResultROS);
        resultRO.setAreFiltersPersisted(areFiltersPersisted);
        return resultRO;
    }

    @RequestMapping(method = RequestMethod.PUT)
    public void updateMessageFilters(@RequestBody List<MessageFilterRO> messageFilterROS) {
        List<BackendFilter> backendFilters = coreConverter.convert(messageFilterROS, BackendFilter.class);
        routingService.updateBackendFilters(backendFilters);
    }

    @RequestMapping(path = "/csv", method = RequestMethod.GET)
    public ResponseEntity<String> getCsv() {
        StringBuilder resultText = new StringBuilder(MessageFilterRO.csvTitle());
        for(MessageFilterRO backendFilter : getBackendFiltersInformation()) {
            resultText.append(backendFilter.toCsvString());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/ms-excel"))
                .header("Content-Disposition", "attachment; filename=messagefilter_datatable.csv")
                .body(resultText.toString());
    }
}
