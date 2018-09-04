package eu.domibus.core.csv;

import java.util.List;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
public interface CsvService {

    String APPLICATION_EXCEL_STR = "application/ms-excel";


    String exportToCSV(List<?> list, Class tClass,
                       final Map<String, String> customColumnNames, List<String> excludedColumns);

    int getMaxNumberRowsToExport();

}
