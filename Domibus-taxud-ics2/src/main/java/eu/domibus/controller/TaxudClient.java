package eu.domibus.controller;

import eu.domibus.plugin.Submission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.activation.DataHandler;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class TaxudClient {

    private final static Logger LOG = LoggerFactory.getLogger(TaxudIcs2Controller.class);

    private static final String APPLICATION_XML = "application/xml";

    private static final String MIME_TYPE = "MimeType";

    private static final String CONTENT_ID = "cid:message";

    private RestTemplate restTemplate;

    private String submissionRestUrl="http://localhost:8080/message";

    public void initTemplate(){
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
    }
    public void sendPayload(){

        Submission submission = restTemplate.getForObject(submissionRestUrl, Submission.class);
        MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
        multipartRequest.add("submissionJson", submission);

        // creating an HttpEntity for the binary part
        String payloadContent = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPGhlbGxvPndvcmxkPC9oZWxsbz4=";
        javax.mail.util.ByteArrayDataSource dataSource = new javax.mail.util.ByteArrayDataSource(payloadContent.getBytes(), APPLICATION_XML);
        dataSource.setName("content.xml");
        DataHandler payLoadDataHandler = new DataHandler(dataSource);
        Submission.TypedProperty submissionTypedProperty = new Submission.TypedProperty(MIME_TYPE, APPLICATION_XML);
        Collection<Submission.TypedProperty> listTypedProperty = new ArrayList<>();
        listTypedProperty.add(submissionTypedProperty);
        Submission.Payload submissionPayload = new Submission.Payload(CONTENT_ID, payLoadDataHandler, listTypedProperty, false, null, null);
        submission.addPayload(submissionPayload);

        for (Submission.Payload payload : submission.getPayloads()) {
            InputStream inputStream;
            byte[] b = new byte[payloadContent.length()];
            try {
                inputStream = payload.getPayloadDatahandler().getInputStream();
                inputStream.read(b);
                if(b.length!=0) {
                    ByteArrayResource byteArrayResource = new ByteArrayResource(b);
                    multipartRequest.add("payload", byteArrayResource);
                }else{
                    LOG.warn("Skip payload");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Map<String, Object> stringObjectMap = multipartRequest.toSingleValueMap();
        Set<Map.Entry<String, Object>> entries = stringObjectMap.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }

        restTemplate.postForLocation(submissionRestUrl, multipartRequest);
    }

    public void sendCertifate(){
        KeyStore trustStore = null;
        try (FileInputStream keyStoreStream = new FileInputStream("C:\\install\\domains\\12.1.3\\red\\conf\\domibus\\keystores\\gateway_keystore.jks")) {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(keyStoreStream, "test123".toCharArray());
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            LOG.error(e.getMessage(),e);
            return;
        }

        byte[] certificate;
        try {
            Certificate blue_gw = trustStore.getCertificate("blue_gw");
            certificate = Base64.getEncoder().encode(blue_gw.getEncoded());
        } catch (KeyStoreException|CertificateEncodingException e) {
            LOG.error(e.getMessage(),e);
            return;
        }
        Submission submission = restTemplate.getForObject(submissionRestUrl, Submission.class);
        MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
        multipartRequest.add("submissionJson", submission);

        ByteArrayResource byteArrayResource =  new ByteArrayResource(certificate);/* {
            @Override
            public String getFilename() {
                return "Test.xml";
            }
        };*/
        multipartRequest.add("certificate", byteArrayResource);

        Boolean aBoolean = restTemplate.postForObject("http://localhost:8080/authenticate", multipartRequest, Boolean.class);
        LOG.info("Authenticated "+aBoolean);

    }

    public static void main(String[] args) {
        TaxudClient taxudClient = new TaxudClient();
        taxudClient.initTemplate();
        taxudClient.sendCertifate();
        taxudClient.sendPayload();
    }
}
