package eu.domibus.web.rest;

import eu.domibus.api.message.UserMessageService;
import eu.domibus.common.dao.MessagingDao;
import eu.domibus.ext.exceptions.MessageMonitorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Created by musatmi on 10/05/2017.
 */
@RestController
@RequestMapping(value = "/rest/message/")
public class MessageResource {

    @Autowired
    UserMessageService userMessageService;


    @RequestMapping(path = "{messageId:.+}/restore", method = RequestMethod.PUT)
    public void resend(@PathVariable(value = "messageId") String messageId) {
        userMessageService.restoreFailedMessage(messageId);
    }

    @RequestMapping(path = "{messageId:.+}/download", method = RequestMethod.GET)
    public ResponseEntity<ByteArrayResource> download(@PathVariable(value = "messageId") String messageId) throws JAXBException {


        final byte[] content = userMessageService.downloadMessage(messageId);
        ByteArrayResource resource = new ByteArrayResource(new byte[0]);
        if (content != null) {
            resource = new ByteArrayResource(content);
        }


        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header("content-disposition", "attachment; filename=" + messageId + ".xml")
                .body(resource);
    }

}
