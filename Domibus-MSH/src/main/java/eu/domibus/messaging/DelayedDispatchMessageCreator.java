package eu.domibus.messaging;

import eu.domibus.api.jms.JmsMessage;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class DelayedDispatchMessageCreator extends DispatchMessageCreator {


    private final long delay;

    public DelayedDispatchMessageCreator(final String messageId, final long delay) {
        super(messageId);
        this.delay = delay;
    }

    public JmsMessage createMessage() {
        JmsMessage m = super.createMessage();
        m.setProperty("AMQ_SCHEDULED_DELAY", delay);
        return m;
    }
}
