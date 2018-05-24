package eu.domibus.util;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.HttpUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by Cosmin Baciu on 12-Jul-16.
 */
@Service
public class HttpUtilImpl implements HttpUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(HttpUtilImpl.class);

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public ByteArrayInputStream downloadURL(String url) throws IOException {
        if (useProxy()) {
            String httpProxyHost = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            String httpProxyPort = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
            String httpProxyUser = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_USER);
            String httpProxyPassword = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_PASSWORD);
            LOG.info("Using proxy for downloading URL " + url);
            return downloadURLViaProxy(url, httpProxyHost, Integer.parseInt(httpProxyPort), httpProxyUser, httpProxyPassword);
        }
        return downloadURLDirect(url);
    }

    @Override
    public boolean useProxy() {
        String useProxy = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_ENABLED, "false");
        if (StringUtils.isEmpty(useProxy)) {
            LOG.debug("Proxy not required. The property domibus.proxy.enabled is not configured");
            return false;
        }

        String httpProxyHost = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
        String httpProxyPort = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
        String httpProxyUser = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_USER);
        String httpProxyPassword = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_PASSWORD);

        if (StringUtils.isEmpty(httpProxyHost) || StringUtils.isEmpty(httpProxyPort)
                || StringUtils.isEmpty(httpProxyUser) || StringUtils.isEmpty(httpProxyPassword)) {

            LOG.error("Proxy is enabled but the configuration is invalid:" + httpProxyHost + " " + httpProxyPort + " " +
                    httpProxyUser);

            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_006, "Proxy is enabled but the configuration is invalid.");
        }
        LOG.info("Proxy configured: " + httpProxyHost + " " + httpProxyPort + " " +
                httpProxyUser);

        return Boolean.parseBoolean(useProxy);
    }

    @Override
    public HttpHost getConfiguredProxy() {
        if (useProxy()) {
            String httpProxyHost = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            String httpProxyPort = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);

            return new HttpHost(httpProxyHost, Integer.parseInt(httpProxyPort));
        }
        return null;
    }

    @Override
    public CredentialsProvider getConfiguredCredentialsProvider() {
        if(useProxy()) {
            String httpProxyHost = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_HOST);
            String httpProxyPort = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_HTTP_PORT);
            String httpProxyUser = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_USER);
            String httpProxyPassword = domibusPropertyProvider.getProperty(DomibusConfigurationService.DOMIBUS_PROXY_PASSWORD);

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(httpProxyHost, Integer.parseInt(httpProxyPort)),
                    new UsernamePasswordCredentials(httpProxyUser, httpProxyPassword));

            return credsProvider;
        }
        return null;
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
