package eu.domibus.common.services;

import java.util.List;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
public interface CsvService {
    String exportToCSV(List<?> list);

    void setExcludedItems(List<String> excludedItems);
}
