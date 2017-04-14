package eu.domibus.api.util.xml;

import java.util.List;

/**
 * Created by Cosmin Baciu on 14-Sep-16.
 */
public interface UnmarshallerResult {

    <T extends Object> T getResult();

    boolean isValid();

    List<String> getErrors();

    String getErrorMessage();
}
