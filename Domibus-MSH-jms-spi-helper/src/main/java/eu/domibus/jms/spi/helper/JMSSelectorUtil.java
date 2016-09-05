package eu.domibus.jms.spi.helper;

import java.util.Map;

/**
 * Created by Cosmin Baciu on 05-Sep-16.
 */
public interface JMSSelectorUtil {

    String getSelector(String messageId);

    String getSelector(String[] messageIds);

    String getSelector(Map<String, Object> criteria);
}
