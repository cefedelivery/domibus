package eu.domibus.jms.spi.helper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
public class JMSSelectorUtilImpl implements JMSSelectorUtil {

    @Override
    public String getSelector(String messageId) {
        StringBuffer selector = new StringBuffer("JMSMessageID = '").append(messageId).append("'");
        return selector.toString();
    }

    @Override
    public String getSelector(String[] messageIds) {
        if (messageIds.length == 1) {
            return getSelector(messageIds[0]);
        }
        StringBuffer selector = new StringBuffer("JMSMessageID IN (");
        for (int i = 0; i < messageIds.length; i++) {
            String messageId = messageIds[i];
            if (i > 0) {
                selector.append(", ");
            }
            selector.append("'").append(messageId).append("'");
        }
        selector.append(")");
        return selector.toString();
    }

    @Override
    public String getSelector(Map<String, Object> criteria) {
        StringBuffer selector = new StringBuffer();
        // JMSType
        String jmsType = (String) criteria.get("JMSType");
        if (!StringUtils.isBlank(jmsType)) {
            selector.append(selector.length() > 0 ? " and " : "");
            selector.append("JMSType='")
                    .append(jmsType.replaceAll("'","''"))
                    .append("'");
        }
        // JMSTimestamp
        Long jmsTimestampFrom = (Long) criteria.get("JMSTimestamp_from");
        if (jmsTimestampFrom != null) {
            selector.append(selector.length() > 0 ? " and " : "");
            selector.append("JMSTimestamp>=").append(jmsTimestampFrom);
        }
        Long jmsTimestampTo = (Long) criteria.get("JMSTimestamp_to");
        if (jmsTimestampTo != null) {
            selector.append(selector.length() > 0 ? " and " : "");
            selector.append("JMSTimestamp<=").append(jmsTimestampTo);
        }
        String selectorClause = (String) criteria.get("selectorClause");
        if (!StringUtils.isBlank(selectorClause)) {
            selector.append(selector.length() > 0 ? " and " : "");
            selector.append(selectorClause);
        }

        return selector.toString().trim();
    }
}
