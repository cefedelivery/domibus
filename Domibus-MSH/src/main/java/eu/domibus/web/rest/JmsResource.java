package eu.domibus.web.rest;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.csv.CsvException;
import eu.domibus.api.jms.JMSManager;
import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.common.services.CsvService;
import eu.domibus.common.services.impl.CsvServiceImpl;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.rest.ro.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/rest/jms")
public class JmsResource {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(JmsResource.class);

    private static final String APPLICATION_JSON = "application/json";

    @Autowired
    JMSManager jmsManager;

    @Autowired
    CsvServiceImpl csvServiceImpl;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Autowired
    private AuthUtils authUtils;

    @RequestMapping(value = {"/destinations"}, method = GET)
    public ResponseEntity<DestinationsResponseRO> destinations() {

        final DestinationsResponseRO destinationsResponseRO = new DestinationsResponseRO();
        try {
            destinationsResponseRO.setJmsDestinations(jmsManager.getDestinations());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(destinationsResponseRO);

        } catch (RuntimeException runEx) {
            LOGGER.error("Error finding the JMS messages sources", runEx);
            return ResponseEntity.badRequest()
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(destinationsResponseRO);
        }
    }

    @RequestMapping(value = {"/messages"}, method = POST)
    public ResponseEntity<MessagesResponseRO> messages(@RequestBody MessagesRequestRO request) {

        final MessagesResponseRO messagesResponseRO = new MessagesResponseRO();
        try {
            if (domibusConfigurationService.isMultiTenantAware() && authUtils.isAdmin()) {
                //get current domain and add it in the selector
                final String domainCode = domainContextProvider.getCurrentDomainSafely().getCode();

                String selector = request.getSelector() != null ? request.getSelector() : StringUtils.EMPTY;
                if (StringUtils.isNotBlank(selector)) {
                    selector += " AND ";
                }
                selector += " DOMAIN='" + domainCode + "'";

                messagesResponseRO.setMessages(jmsManager.browseMessages(request.getSource(), request.getJmsType(), request.getFromDate(), request.getToDate(), selector));
            } else {
                messagesResponseRO.setMessages(jmsManager.browseMessages(request.getSource(), request.getJmsType(), request.getFromDate(), request.getToDate(), request.getSelector()));
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(messagesResponseRO);

        } catch (RuntimeException runEx) {
            LOGGER.error("Error browsing messages for source [" + request.getSource() + "]", runEx);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(messagesResponseRO);
        }
    }

    @RequestMapping(value = {"/messages/action"}, method = POST)
    public ResponseEntity<MessagesActionResponseRO> action(@RequestBody MessagesActionRequestRO request) {

        final MessagesActionResponseRO response = new MessagesActionResponseRO();
        response.setOutcome("Success");

        try {
            List<String> messageIds = request.getSelectedMessages();
            String[] ids = messageIds.toArray(new String[0]);

            if (request.getAction() == MessagesActionRequestRO.Action.MOVE) {
                jmsManager.moveMessages(request.getSource(), request.getDestination(), ids);

            } else if (request.getAction() == MessagesActionRequestRO.Action.REMOVE) {
                jmsManager.deleteMessages(request.getSource(), ids);
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(response);

        } catch (RuntimeException runEx) {
            LOGGER.error("Error performing action [" + request.getAction() + "]", runEx);
            response.setOutcome(runEx.getMessage());
            return ResponseEntity.badRequest()
                    .contentType(MediaType.parseMediaType(APPLICATION_JSON))
                    .body(response);
        }
    }

    /**
     * This method returns a CSV file with the contents of JMS Messages table
     *
     * @return CSV file with the contents of JMS Messages table
     */
    @RequestMapping(path = "/csv", method = RequestMethod.GET)
    public ResponseEntity<String> getCsv(
            @RequestParam(value = "source") String source,
            @RequestParam(value = "jmsType", required = false) String jmsType,
            @RequestParam(value = "fromDate", required = false) Long fromDate,
            @RequestParam(value = "toDate", required = false) Long toDate,
            @RequestParam(value = "selector", required = false) String selector) {
        String resultText;

        // get list of messages
        final List<JmsMessage> jmsMessageList = jmsManager.browseMessages(
                source,
                jmsType,
                fromDate == null ? null : new Date(fromDate),
                toDate == null ? null : new Date(toDate),
                selector);
        customizeJMSProperties(jmsMessageList);

        // excluding unneeded columns
        csvServiceImpl.setExcludedItems(CsvExcludedItems.JMS_RESOURCE.getExcludedItems());

        // needed for empty csv file purposes
        csvServiceImpl.setClass(JmsMessage.class);

        // column name customization
        csvServiceImpl.customizeColumn(CsvCustomColumns.JMS_RESOURCE.getCustomColumns());

        try {
            resultText = csvServiceImpl.exportToCSV(jmsMessageList);
        } catch (CsvException e) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CsvService.APPLICATION_EXCEL_STR))
                .header("Content-Disposition", "attachment; filename=" + csvServiceImpl.getCsvFilename("jms"))
                .body(resultText);
    }

    private void customizeJMSProperties(List<JmsMessage> jmsMessageList) {
        for(JmsMessage message : jmsMessageList) {
            message.setCustomProperties(message.getCustomProperties());
            message.setProperties(message.getJMSProperties());
        }
    }


}
