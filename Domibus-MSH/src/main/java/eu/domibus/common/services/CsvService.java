package eu.domibus.common.services;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
public interface CsvService {
    String exportToCSV(List<?> list);
    void createCSVColumnHeader(StringBuilder result, Field[] fields);
    void createCSVContents(List<?> list, StringBuilder result, Field[] fields);

    void setExcludedItems(List<String> excludedItems);
}
