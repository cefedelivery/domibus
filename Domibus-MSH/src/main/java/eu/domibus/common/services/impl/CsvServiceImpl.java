package eu.domibus.common.services.impl;

import com.google.gson.Gson;
import com.opencsv.CSVWriter;
import eu.domibus.api.csv.CsvException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.util.DomibusStringUtil;
import eu.domibus.common.services.CsvService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@Service
public class CsvServiceImpl implements CsvService {

    private Class<?> theClass = null;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CsvServiceImpl.class);

    private final Map<String, String> customNames = new HashMap<>();

    private List<String> excluded = new ArrayList<>();

    public void setClass(Class<?> tClass) {
        theClass = tClass;
    }

    @Override
    public String exportToCSV(List<?> list) {
        StringWriter result = new StringWriter();
        CSVWriter csvBuilder = new CSVWriter(result);

        List<Field> activeFields = getExportedFields(list);

        createCSVColumnHeader(csvBuilder, activeFields);
        createCSVContents(list, csvBuilder, activeFields);

        return result.toString();
    }

    public void customizeColumn(Map<String, String> customized) {
        customNames.putAll(customized);
    }

    public String getCsvFilename(String module) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        return module + "_datatable_" + dateFormat.format(date) + ".csv";
    }

    public void setExcludedItems(List<String> excludedItems) {
        excluded = excludedItems;
    }

    protected List<Field> getExportedFields(List<?> list) {
        Class<?> clazz;
        if (CollectionUtils.isNotEmpty(list)) {
            clazz = list.get(0).getClass();
        } else if (theClass != null) {
            clazz = theClass;
        } else {
            return new ArrayList<>();
        }

        Field[] fields = clazz.getDeclaredFields();
        List<Field> activeFields = Arrays.stream(fields)
                .filter(field -> !excluded.contains(field.getName()))
                .collect(Collectors.toList());

        return activeFields;
    }

    protected void writeCSVRow(CSVWriter csvBuilder, List<String> values) {
        csvBuilder.writeNext(values.toArray(new String[0]), false);
    }

    protected void createCSVColumnHeader(CSVWriter csvBuilder, List<Field> fields) {
        List<String> fieldValues = new ArrayList<>();
        for (Field field : fields) {
            String varName = field.getName();
            if (customNames.get(varName.toUpperCase()) != null) {
                varName = customNames.get(varName.toUpperCase());
            }
            fieldValues.add(DomibusStringUtil.uncamelcase(varName));
        }
        writeCSVRow(csvBuilder, fieldValues);
    }

    protected void createCSVContents(List<?> list, CSVWriter csvBuilder, List<Field> fields) {
        if (list == null) {
            return;
        }
        for (Object elem : list) {
            List<String> fieldValues = new ArrayList<>();
            // for each field of the class
            for (Field field : fields) {
                // set that field to be accessible
                field.setAccessible(true);
                try {
                    // get the field value
                    String value = serializeFieldValue(field, elem);
                    fieldValues.add(value);
                } catch (IllegalAccessException e) {
                    LOG.error("Exception while writing on CSV ", e);
                    throw new CsvException(DomibusCoreErrorCode.DOM_001, "Exception while writing on CSV", e);
                }
            }
            writeCSVRow(csvBuilder, fieldValues);
        }
    }

    protected String serializeFieldValue(Field field, Object elem) throws IllegalAccessException {
        Object fieldValue = field.get(elem);
        if (fieldValue == null) {
            return StringUtils.EMPTY;
        }
        if (fieldValue instanceof Map) {
            return new Gson().toJson(fieldValue);
        }
        if (fieldValue instanceof Date) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss'GMT'Z");
            ZonedDateTime d = ZonedDateTime.ofInstant(((Date) fieldValue).toInstant(), ZoneId.systemDefault());
            return d.format(f);
        }
        String str = Objects.toString(fieldValue, StringUtils.EMPTY);
        return str;
    }

}
