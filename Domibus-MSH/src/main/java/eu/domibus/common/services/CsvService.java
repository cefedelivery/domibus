package eu.domibus.common.services;

import eu.domibus.common.exception.EbMS3Exception;

import java.util.List;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
public interface CsvService {
    String exportToCSV(List<?> list) throws EbMS3Exception;

    void setExcludedItems(List<String> excludedItems);
}
