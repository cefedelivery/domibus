package eu.domibus.logging.api;

import org.slf4j.Marker;

/**
 * @author Cosmin Baciu
 */
public interface MessageConverter {

  String getMessage(Marker marker, MessageCode key, Object... args);

}
