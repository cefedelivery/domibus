package eu.domibus.dynamicdiscovery;

import eu.domibus.common.exception.ConfigurationException;
import eu.domibus.common.util.EndpointInfo;
import mockit.integration.junit4.JMockit;
import no.difi.vefa.peppol.common.lang.EndpointNotFoundException;
import no.difi.vefa.peppol.common.lang.PeppolLoadingException;
import no.difi.vefa.peppol.common.lang.PeppolParsingException;
import no.difi.vefa.peppol.common.model.*;
import no.difi.vefa.peppol.lookup.LookupClient;
import no.difi.vefa.peppol.lookup.LookupClientBuilder;
import no.difi.vefa.peppol.lookup.api.LookupException;
import no.difi.vefa.peppol.lookup.locator.BusdoxLocator;
import no.difi.vefa.peppol.mode.Mode;
import no.difi.vefa.peppol.security.lang.PeppolSecurityException;
import no.difi.vefa.peppol.security.util.EmptyCertificateValidator;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * @author idragusa
 * @since 6/13/18.
 */

//@RunWith(JMockit.class)
public class PeppolIT {

    //The (sub)domain of the SML, e.g. acc.edelivery.tech.ec.europa.eu
    //private static final String TEST_SML_ZONE = "isaitb.acc.edelivery.tech.ec.europa.eu";
    private static final String TEST_SML_ZONE = "acc.edelivery.tech.ec.europa.eu";

    /* This is not a unit tests but a useful test for a real SMP entry. */
//    @Test
    @Ignore
    public void testLookupInformation() throws Exception {
        EndpointInfo endpoint = testLookupInformation("0088:112233", "iso6523-actorid-upis", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-12::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0", "cenbii-procid-ubl::urn:www.cenbii.eu:profile:bii04:ver1.0", "");
        //EndpointInfo endpoint = testLookupInformation("0088:260420181111", "iso6523-actorid-upis", "urn:oasis:names:specification:ubl:schema:xsd:Invoice-12::Invoice##urn:www.cenbii.eu:transaction:biicoretrdm010:ver1.0:#urn:www.peppol.eu:bis:peppol4a:ver1.0::2.0", "cenbii-procid-ubl::urn:www.cenbii.eu:profile:bii04:ver1.0", "");

        assertNotNull(endpoint);
        System.out.println(endpoint.getAddress());
    }

    private EndpointInfo testLookupInformation(final String participantId, final String participantIdScheme, final String documentId, final String processId, final String processIdScheme) {
        try {
            final LookupClientBuilder lookupClientBuilder = LookupClientBuilder.forMode(Mode.TEST);
            lookupClientBuilder.locator(new BusdoxLocator(TEST_SML_ZONE));
            /* DifiCertificateValidator.validate fails when proxy is enabled */
            if(useProxy) {
                lookupClientBuilder.fetcher(new ApacheFetcherForTest(getConfiguredProxy(), getConfiguredCredentialsProvider()));
                lookupClientBuilder.certificateValidator(EmptyCertificateValidator.INSTANCE);
            } else {
                lookupClientBuilder.fetcher(new ApacheFetcherForTest(null, null));
            }

            final LookupClient smpClient = lookupClientBuilder.build();
            final ParticipantIdentifier participantIdentifier = ParticipantIdentifier.of(participantId, Scheme.of(participantIdScheme));
            final DocumentTypeIdentifier documentIdentifier = DocumentTypeIdentifier.of(documentId);

            final ProcessIdentifier processIdentifier = ProcessIdentifier.parse(processId);
            final ServiceMetadata sm = smpClient.getServiceMetadata(participantIdentifier, documentIdentifier);
            final Endpoint endpoint = sm.getEndpoint(processIdentifier, TransportProfile.AS4);

            if (endpoint == null || endpoint.getAddress() == null) {
                throw new ConfigurationException("Could not fetch metadata from SMP for documentId " + documentId + " processId " + processId);
            }
            return new EndpointInfo(endpoint.getAddress().toString(), endpoint.getCertificate());
        } catch (final PeppolParsingException | PeppolLoadingException | PeppolSecurityException | LookupException | EndpointNotFoundException | IllegalStateException e) {
            throw new ConfigurationException("Could not fetch metadata from SMP for documentId " + documentId + " processId " + processId, e);
        }
    }
    Boolean useProxy = false;
    String httpProxyHost = "";
    String httpProxyPort = "";
    String httpProxyUser = "";
    String httpProxyPassword = "";


    private HttpHost getConfiguredProxy() {
        return new HttpHost(httpProxyHost, Integer.parseInt(httpProxyPort));
    }

    private CredentialsProvider getConfiguredCredentialsProvider() {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(httpProxyHost, Integer.parseInt(httpProxyPort)),
                new UsernamePasswordCredentials(httpProxyUser, httpProxyPassword));

        return credsProvider;
    }

}