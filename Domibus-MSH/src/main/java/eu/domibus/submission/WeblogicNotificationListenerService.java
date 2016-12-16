/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.submission;

import eu.domibus.plugin.BackendConnector;
import eu.domibus.plugin.NotificationListenerService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.jms.JMSException;
import javax.jms.Queue;
import java.util.Properties;

/**
 * @author Christian Koch, Stefan Mueller
 */

public class WeblogicNotificationListenerService extends NotificationListenerService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WeblogicNotificationListenerService.class);

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;
    private String queueJndi;

    public WeblogicNotificationListenerService(final Queue queue, final BackendConnector.Mode mode) {
        super(queue, mode);
    }

    protected String getQueueName(Queue queue) throws JMSException {
//        String jmsServerName = domibusProperties.getProperty("weblogic.jmsServerName");
        String queueName = queueJndi;
//        if (!StringUtils.isEmpty(jmsServerName)) {
//            queueName = jmsServerName + "/" + queue.getQueueName();
//        } else {
//            queueName = super.getQueueName(queue);
//        }
        LOG.info("getQueueName for [" + queue.getQueueName() + "] = " + queueName);
        return queueName;
    }

    public String getQueueJndi() {
        return queueJndi;
    }

    public void setQueueJndi(String queueJndi) {
        this.queueJndi = queueJndi;
    }
}
