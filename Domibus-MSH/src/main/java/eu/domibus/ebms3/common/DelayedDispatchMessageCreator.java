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

package eu.domibus.ebms3.common;

import org.apache.activemq.ScheduledMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class DelayedDispatchMessageCreator extends DispatchMessageCreator {


    private final long delay;

    public DelayedDispatchMessageCreator(final String messageId, final String endpoint, final long delay) {
        super(messageId, endpoint);
        this.delay = delay;
    }

    @Override
    public Message createMessage(final Session session) throws JMSException {
        final Message m = super.createMessage(session);
        m.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
        return m;
    }
}
