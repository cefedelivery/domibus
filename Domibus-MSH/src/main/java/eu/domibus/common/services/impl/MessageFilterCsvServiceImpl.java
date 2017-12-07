package eu.domibus.common.services.impl;

import eu.domibus.api.routing.RoutingCriteria;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.web.rest.ro.MessageFilterRO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@Service
public class MessageFilterCsvServiceImpl extends CsvServiceImpl {

    private List<String> csvHeader = new ArrayList<>();

    private String[] routingCriteriasArray = {"From", "To", "Action", "Service"};

    MessageFilterCsvServiceImpl() {
        csvHeader.add("Backend Name");
        csvHeader.addAll(Arrays.asList(routingCriteriasArray));
        csvHeader.add("Persisted");
    }

    private String getCsvHeader() {
        return new StringBuilder()
                .append(csvHeader)
                .append(System.lineSeparator())
                .toString();
    }

    private String routingCriteriasToCsvString(List<RoutingCriteria> routingCriterias) {
        // I don't like this approach but we have a fixed table for Routing Criterias and we need to keep this order always
        // even if routing criterias exist on a different order in backend filter object
        String[] result = new String[routingCriteriasArray.length];
        for(RoutingCriteria rc : routingCriterias) {
            for(int i = 0; i < routingCriteriasArray.length; i++) {
                if(rc.getName().equalsIgnoreCase(routingCriteriasArray[i])) {
                    result[i] = Objects.toString(rc.getExpression(),"");
                } else {
                    result[i] = "";
                }
            }
        }
        return Arrays.toString(result);
    }

    private String toCsvString(MessageFilterRO messageFilterRO) {
        return new StringBuilder()
                .append(Objects.toString(messageFilterRO.getBackendName(),"")).append(",")
                .append(routingCriteriasToCsvString(messageFilterRO.getRoutingCriterias())).append(",")
                .append(Objects.toString(messageFilterRO.isPersisted(), ""))
                .append(System.lineSeparator())
                .toString();
    }

    @Override
    public String exportToCSV(List<?> list) throws EbMS3Exception {
        if(list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        result.append(getCsvHeader());
        for (Object messageFilterRO : list) {
            result.append(toCsvString((MessageFilterRO) messageFilterRO));
        }
        return result.toString().replace("[","").replace("]", "");
    }
}
