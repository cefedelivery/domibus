package eu.domibus.web.rest;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.common.dao.UserMessageLogDao;
import eu.domibus.core.message.MessageConverterService;
import eu.domibus.ebms3.common.model.*;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

/**
 * @author Tiago Miguel
 * @since 3.3
 */
@RunWith(JMockit.class)
public class MessageResourceTest {

    @Tested
    MessageResource messageResource;

    @Injectable
    UserMessageService userMessageService;

    @Injectable
    MessageConverterService messageConverterService;

    @Injectable
    private MessagingDao messagingDao;

    @Injectable
    private UserMessageLogDao userMessageLogDao;

    private UserMessage createUserMessage() {
        UserMessage userMessage = new UserMessage();
        userMessage.setEntityId(1);
        userMessage.setMessageInfo(new MessageInfo());
        userMessage.setCollaborationInfo(new CollaborationInfo());
        userMessage.setMessageProperties(new MessageProperties());
        userMessage.setMpc("mpc1");
        userMessage.setPartyInfo(new PartyInfo());
        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = new PartInfo();
        partInfo.setHref("href");
        byte[] byteA = new byte[]{1,0,1};
        partInfo.setBinaryData(byteA);
        payloadInfo.getPartInfo().add(partInfo);
        userMessage.setPayloadInfo(payloadInfo);
        return userMessage;
    }

    @Test
    public void testDownload() {
        // Given
        new Expectations() {{
            messagingDao.findUserMessageByMessageId(anyString);
            result = createUserMessage();
        }};

        // When
        ResponseEntity<ByteArrayResource> responseEntity = messageResource.download("messageId");

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(2, responseEntity.getHeaders().size());
        Assert.assertEquals("application/octet-stream", responseEntity.getHeaders().get("Content-Type").get(0));
        Assert.assertEquals("attachment; filename=messageId.xml", responseEntity.getHeaders().get("content-disposition").get(0));
        Assert.assertEquals("Byte array resource [resource loaded from byte array]", responseEntity.getBody().getDescription());
    }

    @Test
    @Ignore
    public void testZipFiles() {
        // Given
        new Expectations() {{
            messagingDao.findUserMessageByMessageId(anyString);
            result = createUserMessage();
        }};

        ResponseEntity<ByteArrayResource> responseEntity = null;
        try {
            // When
            responseEntity = messageResource.zipFiles("messageId");
        } catch (IOException e) {
            // NOT Then :)
            Assert.fail("Exception in zipFiles method");
        }

        // Then
        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals("application/zip", responseEntity.getHeaders().get("Content-Type").get(0));
        Assert.assertEquals("attachment; filename=messageId.zip", responseEntity.getHeaders().get("content-disposition").get(0));
        Assert.assertEquals("Byte array resource [resource loaded from byte array]", responseEntity.getBody().getDescription());
    }
}
