package eu.domibus.api.util;

import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;



/**
 * Created by Cosmin Baciu on 13-Jul-16.
 */
public interface HttpUtil {
    ByteArrayInputStream downloadURL(String url) throws IOException;

    ByteArrayInputStream downloadURLDirect(String url) throws IOException;

    ByteArrayInputStream downloadURLViaProxy(String url, String proxyHost, Integer proxyPort, String proxyUser, String proxyPassword) throws IOException;

    boolean useProxy();

    HttpHost getConfiguredProxy();

    CredentialsProvider getConfiguredCredentialsProvider();

}
