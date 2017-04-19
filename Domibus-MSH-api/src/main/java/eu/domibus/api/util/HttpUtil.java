package eu.domibus.api.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by Cosmin Baciu on 13-Jul-16.
 */
public interface HttpUtil {
    ByteArrayInputStream downloadURL(String url) throws IOException;

    ByteArrayInputStream downloadURLDirect(String url) throws IOException;

    ByteArrayInputStream downloadURLViaProxy(String url, String proxyHost, Integer proxyPort, String proxyUser, String proxyPassword) throws IOException;
}
