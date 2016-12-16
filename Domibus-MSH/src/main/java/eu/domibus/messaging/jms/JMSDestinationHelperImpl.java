package eu.domibus.messaging.jms;

import eu.domibus.api.jms.JMSDestinationHelper;
import eu.domibus.api.regex.RegexUtil;
import org.apache.commons.lang.StringUtils;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Properties;

/**
 * Created by Cosmin Baciu on 31-Aug-16.
 */
@Component
public class JMSDestinationHelperImpl implements JMSDestinationHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(JMSDestinationHelperImpl.class);

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
