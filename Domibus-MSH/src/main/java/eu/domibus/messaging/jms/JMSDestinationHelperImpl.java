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
 * Created by Cosmin Baciu on 31-Aug-16.
 */
@Component
public class JMSDestinationHelperImpl implements JMSDestinationHelper {

    private static final Log LOG = LogFactory.getLog(JMSDestinationHelperImpl.class);

    private static final String INTERNALQUEUE_EXPRESSION = "domibus.jms.internalQueue.expression";

    @Resource(name = "domibusProperties")
    Properties domibusProperties;

    @Autowired
    RegexUtil regexUtil;

    @Override
    public boolean isInternal(String name) {
        String internalQueueExpression = domibusProperties.getProperty(INTERNALQUEUE_EXPRESSION);
        if (StringUtils.isEmpty(internalQueueExpression)) {
            LOG.warn("Could not determine internal queues: property [" + INTERNALQUEUE_EXPRESSION + "] is not defined" );
            return false;
        }
        return regexUtil.matches(internalQueueExpression, name);
    }
}
