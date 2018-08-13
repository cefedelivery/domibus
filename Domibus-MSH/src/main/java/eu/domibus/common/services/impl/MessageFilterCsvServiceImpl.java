package eu.domibus.common.services.impl;

import com.opencsv.CSVWriter;
import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.web.rest.ro.MessageFilterRO;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@Service
public class MessageFilterCsvServiceImpl extends CsvServiceImpl {

    private List<String> csvHeader = new ArrayList<>();

    private String[] routingCriteriaArray = {"From", "To", "Action", "Service"};

    public MessageFilterCsvServiceImpl() {
        csvHeader.add("Plugin");
        csvHeader.addAll(Arrays.asList(routingCriteriaArray));
        csvHeader.add("Persisted");
    }

    @Override
    public String exportToCSV(List<?> list) {
        StringWriter result = new StringWriter();
        CSVWriter csvBuilder = new CSVWriter(result);

        writeCSVRow(csvBuilder, csvHeader);

        if (list != null && !list.isEmpty()) {
            for (Object messageFilterRO : list) {
                writeCSVRow(csvBuilder, getCsvRow((MessageFilterRO) messageFilterRO));
            }
        }
        return result.toString();
    }

    private List<String> getCsvRow(MessageFilterRO messageFilterRO) {
        List<String> values = new ArrayList<>();
        values.add(Objects.toString(messageFilterRO.getBackendName(), ""));
        values.addAll(routingCriteriaToCsvString(messageFilterRO.getRoutingCriterias()));
        values.add(Objects.toString(messageFilterRO.isPersisted(), ""));
        return values;
    }

    private List<String> routingCriteriaToCsvString(List<RoutingCriteria> routingCriteria) {
        return Arrays.stream(routingCriteriaArray).map(name -> {
            RoutingCriteria rc = routingCriteria.stream()
                    .filter(x -> x.getName().equalsIgnoreCase(name))
                    .findFirst().orElse(null);
            return rc == null ? "" : Objects.toString(rc.getExpression(), "");
        }).collect(Collectors.toList());
    }
}
