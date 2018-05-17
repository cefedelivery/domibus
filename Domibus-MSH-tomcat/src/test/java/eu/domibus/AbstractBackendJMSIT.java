package eu.domibus;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public abstract class AbstractBackendJMSIT extends AbstractIT {

    protected static final String JMS_NOT_QUEUE_NAME = "domibus.notification.jms";

    protected static final String JMS_BACKEND_IN_QUEUE_NAME = "domibus.backend.jms.inQueue";

    protected static final String JMS_BACKEND_OUT_QUEUE_NAME = "domibus.backend.jms.outQueue";

    protected static final String JMS_BACKEND_REPLY_QUEUE_NAME = "domibus.backend.jms.replyQueue";
}
