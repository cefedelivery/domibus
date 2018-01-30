package eu.domibus.common.services.impl;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.util.DomibusStringUtil;
import eu.domibus.common.services.CsvService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@Service
public class CsvServiceImpl<T> implements CsvService {

    private Class<T> theClass = null;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CsvServiceImpl.class);

    private final Map<String, String> customNames = new HashMap<>();

    private List<String> excluded = new ArrayList<>();

    public void setClass(Class<T> tClass) {
        theClass = tClass;
    }

    @Override
    public String exportToCSV(List<?> list) {
        StringBuilder result = new StringBuilder();
        Field[] fields;
        if(CollectionUtils.isNotEmpty(list)) {
            final Class<?> aClass = list.get(0).getClass();
            fields = aClass.getDeclaredFields();
            createCSVColumnHeader(result, fields);
            createCSVContents(list, result, fields);
        } else {
            if (theClass != null) {
                fields = theClass.getDeclaredFields();
                createCSVColumnHeader(result, fields);
            }
        }

        return result.toString();
    }

    @Override
    public void createCSVColumnHeader(StringBuilder result, Field[] fields) {
        for(Field field : fields) {
            String varName = field.getName();
            if(customNames.get(varName.toUpperCase()) != null) {
                varName = customNames.get(varName.toUpperCase());
            }
            if (excluded.contains(varName)) {
                continue;
            }
            result.append(DomibusStringUtil.uncamelcase(varName));
            result.append(COMMA);
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
                    // get the field value
                    String fieldValue = Objects.toString(field.get(elem), StringUtils.EMPTY);
                    // if field contains ,(comma) we should include ""
                    if(fieldValue.contains(COMMA)) {
                        fieldValue = DOUBLE_QUOTES + fieldValue + DOUBLE_QUOTES;
                    }
                    result.append(fieldValue);
                    result.append(COMMA);
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

    public void customizeColumn(Map<String, String> customized) {
        customNames.putAll(customized);
    }

    public String getCsvFilename(String module) {
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
        return module + "_datatable_" + dateFormat.format(date) + ".csv";
    }

    public void setExcludedItems(List<String> excludedItems) {
        excluded = excludedItems;
    }
}
