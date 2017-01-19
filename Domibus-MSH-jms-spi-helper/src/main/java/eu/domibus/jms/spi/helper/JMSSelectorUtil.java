package eu.domibus.jms.spi.helper;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
public interface JMSSelectorUtil {

    String getSelector(String messageId);

    String getSelector(String[] messageIds);

    String getSelector(Map<String, Object> criteria);
}
