package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.api.regex.RegexUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Properties;

/**
 * @author Cosmin Baciu
 * @since 3.2
 */
@Component
public class JMSDestinationHelperImpl implements JMSDestinationHelper {

    private static final Log LOG = LogFactory.getLog(JMSDestinationHelperImpl.class);

    private static final String DEFAULT_INTERNAL_QUEUE_EXPRESSION = ".*domibus\\.(internal|DLQ|backend\\.jms|notification\\.jms|notification\\.webservice|notification\\.kerkovi).*";
    private static final String INTERNALQUEUE_EXPRESSION = "domibus.jms.internalQueue.expression";

    @Resource(name = "domibusProperties")
    Properties domibusProperties;

    @Autowired
    RegexUtil regexUtil;

    @Override
    public boolean isInternal(String name) {
        String internalQueueExpression = domibusProperties.getProperty(INTERNALQUEUE_EXPRESSION);
        if (StringUtils.isEmpty(internalQueueExpression)) {
            LOG.info("Property [" + INTERNALQUEUE_EXPRESSION + "] is not defined: using the default expression [" + DEFAULT_INTERNAL_QUEUE_EXPRESSION + "]");
            internalQueueExpression = DEFAULT_INTERNAL_QUEUE_EXPRESSION;
        }
        return regexUtil.matches(internalQueueExpression, name);
    }
}
