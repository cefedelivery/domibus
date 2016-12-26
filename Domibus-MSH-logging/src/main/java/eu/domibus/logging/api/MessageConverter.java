package eu.domibus.logging.api;

import org.slf4j.Marker;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public interface MessageConverter {

  String getMessage(Marker marker, MessageCode key, Object... args);

}
