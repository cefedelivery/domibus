package eu.domibus.common.util;

import com.google.common.io.ByteStreams;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import no.difi.vefa.peppol.lookup.api.FetcherResponse;
import no.difi.vefa.peppol.lookup.api.LookupException;
import no.difi.vefa.peppol.lookup.fetcher.AbstractFetcher;
import no.difi.vefa.peppol.mode.Mode;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * @author idragusa
 * @since 5/22/18.
 */
/*
* This class is inherited from the BasicApacheFetcher of the peppol-lookup, to allow
* the configuration of the proxy (if enabled)
*
* */
public class DomibusApacheFetcher extends AbstractFetcher {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusApacheFetcher.class);

    protected RequestConfig requestConfig;

    protected ProxyUtil proxyUtil;

    public DomibusApacheFetcher(Mode mode, ProxyUtil proxyUtil) {
        super(mode);

        this.proxyUtil = proxyUtil;

        LOG.debug("Create RequestConfig");
        RequestConfig.Builder builder = RequestConfig.custom()
                .setConnectionRequestTimeout(timeout)
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout);

        builder.setProxy(proxyUtil.getConfiguredProxy());

        requestConfig = builder.build();
    }

    @Override
    public FetcherResponse fetch(URI uri) throws LookupException, FileNotFoundException {
        LOG.debug("Fetch response from URI [{}]", uri);
        try (CloseableHttpClient httpClient = createClient()) {
            HttpGet httpGet = new HttpGet(uri);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                switch (response.getStatusLine().getStatusCode()) {
                    case 200:
                        return new FetcherResponse(
                                new ByteArrayInputStream(ByteStreams.toByteArray(response.getEntity().getContent())),
                                // new BufferedInputStream(response.getEntity().getContent()),
                                response.containsHeader("X-SMP-Namespace") ?
                                        response.getFirstHeader("X-SMP-Namespace").getValue() : null
                        );
                    case 404:
                        throw new FileNotFoundException(uri.toString());
                    default:
                        throw new LookupException(String.format(
                                "Received code %s for lookup. URI: %s", response.getStatusLine().getStatusCode(), uri));
                }
            }
        } catch (SocketTimeoutException | SocketException | UnknownHostException e) {
            throw new LookupException(String.format("Unable to fetch '%s'", uri), e);
        } catch (LookupException | FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new LookupException(e.getMessage(), e);
        }
    }

    protected CloseableHttpClient createClient() {
        LOG.debug("Create http client");
        HttpClientBuilder builder = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig);

        builder.setDefaultCredentialsProvider(proxyUtil.getConfiguredCredentialsProvider());

        return builder.build();
    }
}
