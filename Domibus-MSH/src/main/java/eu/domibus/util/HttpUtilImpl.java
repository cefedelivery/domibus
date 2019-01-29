package eu.domibus.util;

import eu.domibus.api.util.HttpUtil;
import eu.domibus.common.util.ProxyUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.proxy.DomibusProxyService;
import eu.domibus.proxy.DomibusProxyServiceImpl;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
    DomibusProxyService domibusProxyService;

    @Autowired
    ProxyUtil proxyUtil;

    @Override
    public ByteArrayInputStream downloadURL(String url) throws IOException {
        if (domibusProxyService.useProxy()) {
            return downloadURLViaProxy(url);
        }
        return downloadURLDirect(url);
    }

    @Override
    public ByteArrayInputStream downloadURLDirect(String url) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        try {
            LOG.debug("Executing request " + httpGet.getRequestLine() + " directly");
            return getByteArrayInputStream(httpclient, httpGet);
        } finally {
            httpclient.close();
        }
    }

    @Override
    public ByteArrayInputStream downloadURLViaProxy(String url) throws IOException {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        CredentialsProvider credentialsProvider = proxyUtil.getConfiguredCredentialsProvider();
        if(credentialsProvider != null) {
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }

        try(CloseableHttpClient httpClient = httpClientBuilder.build()) {
            HttpHost proxy = proxyUtil.getConfiguredProxy();

            RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
            HttpGet httpGet = new HttpGet(url);
            httpGet.setConfig(config);

            LOG.debug("Executing request " + httpGet.getRequestLine() + " via " + proxy);
            return getByteArrayInputStream(httpClient, httpGet);
        }
    }

    private ByteArrayInputStream getByteArrayInputStream(CloseableHttpClient httpclient, HttpGet httpGet) throws IOException {
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpGet);
            return new ByteArrayInputStream(IOUtils.toByteArray(response.getEntity().getContent()));
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

}