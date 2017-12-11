package eu.domibus.common.services.impl;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.util.DomibusStringUtil;
import eu.domibus.common.services.CsvService;
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
public class CsvServiceImpl implements CsvService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CsvServiceImpl.class);

    private List<String> excluded = new ArrayList<>();

    @Override
    public String exportToCSV(List<?> list) {
        if(list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        final Class<?> aClass = list.get(0).getClass();
        Field[] fields = aClass.getDeclaredFields();

        createCSVColumnHeader(result, fields);
        createCSVContents(list, result, fields);

        return result.toString();
    }

    @Override
    public void createCSVColumnHeader(StringBuilder result, Field[] fields) {
        for(Field field : fields) {
            final String varName = field.getName();
            if (excluded.contains(varName)) {
                continue;
            }
            result.append(DomibusStringUtil.uncamelcase(varName));
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        result.append(System.lineSeparator());
    }

    @Override
    public void createCSVContents(List<?> list, StringBuilder result, Field[] fields) {
        for(Object elem : list) {
            // for each field of the class
            for (Field field : fields) {
                // if it's not on the list of the excluded ones
                if (excluded.contains(field.getName())) {
                    continue;
                }
                // set that field to be accessible
                field.setAccessible(true);
                try {
                    // get the vield value
                    String fieldValue = Objects.toString(field.get(elem), "");
                    // if field contains ,(comma) we should include ""
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
            // delete the last ,(comma)
            result.deleteCharAt(result.length() - 1);
            result.append(System.lineSeparator());
        }
    }

    public void setExcludedItems(List<String> excludedItems) {
        excluded = excludedItems;
    }
}
