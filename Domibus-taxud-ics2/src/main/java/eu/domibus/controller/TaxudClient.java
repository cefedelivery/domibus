package eu.domibus.controller;

import eu.domibus.plugin.Submission;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.activation.DataHandler;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class TaxudClient {

    public static void main(String[] args) {
new TaxudClient().sendSubmission();
    }

    private static final String APPLICATION_XML = "application/xml";
    private static final String MIME_TYPE = "MimeType";
    private static final String CONTENT_ID = "cid:message";

    public void sendSubmission(){

        String submissionRestUrl="http://localhost:8080/message";


        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter());
       // restTemplate.getMessageConverters().add(new ResourceHttpMessageConverter());

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
                    ByteArrayResource byteArrayResource = new ByteArrayResource(b) {
                        @Override
                        public String getFilename() {
                            return "Test.xml";
                        }
                    };
                    multipartRequest.add("payload", byteArrayResource);
                }else{
                    System.out.println("Skip payload");
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

    public void test(){

    }
}
