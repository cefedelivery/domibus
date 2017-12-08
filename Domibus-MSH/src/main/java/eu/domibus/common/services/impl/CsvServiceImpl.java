package eu.domibus.common.services.impl;

import eu.domibus.api.csv.CsvException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.util.DomibusStringUtil;
import eu.domibus.common.services.CsvService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * @author Tiago Miguel
 * @since 4.0
 */

@Service
public class CsvServiceImpl implements CsvService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CsvServiceImpl.class);

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
            result.append(DomibusStringUtil.uncamelcase(varName));
            result.append(",");
        }
        result.deleteCharAt(result.length() - 1);
        result.append(System.lineSeparator());
    }

    @Override
    public void createCSVContents(List<?> list, StringBuilder result, Field[] fields) {
        for(Object elem : list) {
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    result.append(Objects.toString(field.get(elem), ""));
                    result.append(",");
                } catch (IllegalAccessException e) {
                    LOG.error("Exception while writing on CSV ", e);
                    throw new CsvException(DomibusCoreErrorCode.DOM_001, "Exception while writing on CSV", e);
                }
            }
            result.deleteCharAt(result.length() - 1);
            result.append(System.lineSeparator());
        }
    }
}
