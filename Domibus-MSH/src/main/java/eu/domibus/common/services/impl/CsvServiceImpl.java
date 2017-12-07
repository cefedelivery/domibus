package eu.domibus.common.services.impl;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@Service
public class CsvServiceImpl extends CsvServiceAbstract {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CsvServiceImpl.class);

    private List<String> excluded = new ArrayList<>();

    @Override
    public String exportToCSV(List<?> list) {
        if(list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        final Class<?> aClass = list.get(0).getClass();
        // Column Header
        Field[] fields = aClass.getDeclaredFields();
        for(Field field : fields) {
            final String varName = field.getName();
            if (excluded.contains(varName)) {
                continue;
            }
            result.append(uncamelcase(varName));
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        result.append(System.lineSeparator());

        // CSV contents
        for(Object elem : list) {
            for (Field field : fields) {
                if (excluded.contains(field.getName())) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    String fieldValue = Objects.toString(field.get(elem), "");
                    if(fieldValue.contains(",")) {
                        fieldValue = "\"" + fieldValue + "\"";
                    }
                    result.append(fieldValue);
                    result.append(",");
                } catch (IllegalAccessException e) {
                    LOG.error("Exception while writing on CSV ", e);
                    throw new CsvException(DomibusCoreErrorCode.DOM_001, "Exception while writing on CSV", e);
                }
            }
            result.deleteCharAt(result.length() - 1);
            result.append(System.lineSeparator());
        }
        return result.toString();
    }

    public void setExcludedItems(List<String> excludedItems) {
        excluded = excludedItems;
    }
}
