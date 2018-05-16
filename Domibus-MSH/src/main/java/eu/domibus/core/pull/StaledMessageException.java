package eu.domibus.core.pull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaledMessageException extends RuntimeException{

    private String messageId;

    public StaledMessageException(final String message) {
        super(message);
    }
}
