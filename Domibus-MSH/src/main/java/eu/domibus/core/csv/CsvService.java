package eu.domibus.core.csv;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
public interface CsvService {

    String APPLICATION_EXCEL_STR = "application/ms-excel";

    int MAX_NUMBER_OF_ENTRIES = 10000;

    String exportToCSV(List<?> list, Class tClass,
                       final Map<String, String> customColumnNames, List<String> excludedColumns);

//    String getCsvFilename(String module);
//    void setClass(Class<?> tClass);
//    void setExcludedItems(List<String> excludedItems);

}
