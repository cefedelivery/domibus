package eu.domibus.messaging;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.api.multitenancy.Domain;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class DelayedDispatchMessageCreator extends DispatchMessageCreator {


    private final long delay;

    public DelayedDispatchMessageCreator(final String messageId, final Domain domain, final long delay) {
        super(messageId, domain);
        this.delay = delay;
    }

    public JmsMessage createMessage() {
        JmsMessage m = super.createMessage();
        m.setProperty("AMQ_SCHEDULED_DELAY", delay);
        return m;
    }
}
