package eu.domibus.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Cosmin Baciu on 12-Jul-16.
 */
@Service
public class HttpUtilImpl implements HttpUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtilImpl.class);

    @Autowired
    @Qualifier("domibusProperties")
    private Properties domibusProperties;

    @Override
    public ByteArrayInputStream downloadURL(String url) throws IOException {
        if (useProxy()) {
            String httpProxyHost = domibusProperties.getProperty("domibus.proxy.http.host");
            String httpProxyPort = domibusProperties.getProperty("domibus.proxy.http.port");
            String httpProxyUser = domibusProperties.getProperty("domibus.proxy.user");
            String httpProxyPassword = domibusProperties.getProperty("domibus.proxy.password");
            //TODO
            String httpNonProxyHosts = domibusProperties.getProperty("domibus.proxy.nonProxyHosts");
            LOG.info("Using proxy for downloading URL " + url);
            return downloadURLViaProxy(url, httpProxyHost, Integer.parseInt(httpProxyPort), httpProxyUser, httpProxyPassword);
        }
        return downloadURLDirect(url);
    }

    protected boolean useProxy() {
        String useProxy = domibusProperties.getProperty("domibus.proxy.enabled", "false");
        if (StringUtils.isEmpty(useProxy)) {
            LOG.debug("Proxy not required. The property domibus.proxy.enabled is not configured");
            return false;
        }
        return Boolean.parseBoolean(useProxy);
    }

    @Override
    public ByteArrayInputStream downloadURLDirect(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        try {
            CloseableHttpResponse response = null;
            try {
                response = httpclient.execute(httpGet);
                return new ByteArrayInputStream(IOUtils.toByteArray(response.getEntity().getContent()));
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } finally {
            httpclient.close();
        }
    }

    @Override
    public ByteArrayInputStream downloadURLViaProxy(String url, String proxyHost, Integer proxyPort, String proxyUser, String proxyPassword) throws IOException {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(proxyHost, proxyPort),
                new UsernamePasswordCredentials(proxyUser, proxyPassword));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider).build();
        try {
            HttpHost proxy = new HttpHost(proxyHost, proxyPort);

            RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
            HttpGet httpget = new HttpGet(url);
            httpget.setConfig(config);

            LOG.debug("Executing request " + httpget.getRequestLine() + " via " + proxy);

            CloseableHttpResponse response = null;
            try {
                response = httpclient.execute(httpget);
                return new ByteArrayInputStream(IOUtils.toByteArray(response.getEntity().getContent()));
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } finally {
            httpclient.close();
        }
    }


}
