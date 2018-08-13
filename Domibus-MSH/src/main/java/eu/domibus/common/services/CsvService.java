package eu.domibus.common.services;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
public interface CsvService {

    String APPLICATION_EXCEL_STR = "application/ms-excel";

    int MAX_NUMBER_OF_ENTRIES = 10000;

    void setClass(Class<?> tClass);

    void setExcludedItems(List<String> excludedItems);

    String exportToCSV(List<?> list);

    String getCsvFilename(String module);

}
