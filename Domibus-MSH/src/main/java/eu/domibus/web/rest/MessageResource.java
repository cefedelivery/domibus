package eu.domibus.web.rest;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.core.message.MessageConverterService;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.common.model.PartInfo;
import eu.domibus.ebms3.common.model.UserMessage;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by musatmi on 10/05/2017.
 */
@RestController
@RequestMapping(value = "/rest/message/")
public class MessageResource {

    @Autowired
    UserMessageService userMessageService;

    @Autowired
    MessageConverterService messageConverterService;

    @Autowired
    private MessagingDao messagingDao;


    @RequestMapping(path = "{messageId:.+}/restore", method = RequestMethod.PUT)
    public void resend(@PathVariable(value = "messageId") String messageId) {
        userMessageService.restoreFailedMessage(messageId);
    }

    @RequestMapping(path = "{messageId:.+}/downloadOld", method = RequestMethod.GET)
    public ResponseEntity<ByteArrayResource> download(@PathVariable(value = "messageId") String messageId) {

        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        final byte[] content = getMessage(userMessage);

        ByteArrayResource resource = new ByteArrayResource(new byte[0]);
        if (content != null) {
            resource = new ByteArrayResource(content);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("content-disposition", "attachment; filename=" + messageId + ".xml")
                .body(resource);
    }

    @RequestMapping(value = "{messageId:.+}/download")
    public ResponseEntity<ByteArrayResource> zipFiles(@PathVariable(value = "messageId") String messageId) throws IOException {

        UserMessage userMessage = messagingDao.findUserMessageByMessageId(messageId);
        final Map<String, byte[]> message = getMessageWithAttachments(userMessage);
        byte[] zip = zip(message);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header("content-disposition", "attachment; filename=" + messageId + ".zip")
                .body(new ByteArrayResource(zip));

    }

    public byte[] getMessage(UserMessage userMessage) {

        Messaging message = new Messaging();
        message.setUserMessage(userMessage);

        return messageConverterService.getAsByteArray(message);
    }

    private Map<String, byte[]> getMessageWithAttachments(UserMessage userMessage) {

        Map<String, byte[]> ret = new HashMap<>();

        final Set<PartInfo> partInfo = userMessage.getPayloadInfo().getPartInfo();
        for (PartInfo info : partInfo) {
            ret.put(info.getHref().replace("cid:",""), info.getBinaryData());
        }

        ret.put("message.xml", getMessage(userMessage));


        return ret;
    }

    private byte[] zip(Map<String, byte[]> message) throws IOException {
        //creating byteArray stream, make it bufforable and passing this buffor to ZipOutputStream
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
             ZipOutputStream zipOutputStream = new ZipOutputStream(bufferedOutputStream)) {

            for (Map.Entry<String, byte[]> entry : message.entrySet()) {
                zipOutputStream.putNextEntry(new ZipEntry(entry.getKey()));

                IOUtils.copy(new ByteArrayInputStream(entry.getValue()), zipOutputStream);

                zipOutputStream.closeEntry();
            }

            zipOutputStream.finish();
            zipOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }

}
