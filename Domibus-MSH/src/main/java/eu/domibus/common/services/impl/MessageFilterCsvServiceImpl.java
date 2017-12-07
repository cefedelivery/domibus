package eu.domibus.common.services.impl;

import eu.domibus.api.routing.RoutingCriteria;
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
public class MessageFilterCsvServiceImpl extends CsvServiceAbstract {

    private List<String> csvHeader = new ArrayList<>();

    private String[] routingCriteriasArray = {"From", "To", "Action", "Service"};

    public MessageFilterCsvServiceImpl() {
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
        List<String> result = new ArrayList<>(4);
        for(RoutingCriteria rc : routingCriterias) {
            for(int i = 0; i < routingCriteriasArray.length; i++) {
                if(rc.getName().equalsIgnoreCase(routingCriteriasArray[i])) {
                    result.add(i, Objects.toString(rc.getExpression(),""));
                } else {
                    result.add(i, "");
                }
            }
        }
        return result.toString();
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
    public String exportToCSV(List<?> list) {
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

    @Override
    public void setExcludedItems(List<String> excludedItems) {
    }
}
