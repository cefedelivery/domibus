package eu.domibus.plugin.fs.queue;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * FS Plugin Send Queue message listener
 *
 * @author Catalin Enache
 * @since 4.1
 */
@Service("fsSendMessageListener")
public class FSSendMessageListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSSendMessageListener.class);

    @Autowired
    private FSSendMessagesService fsSendMessagesService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void onMessage(Message message) {
        LOG.debug("received message on fsPluginSendQueue");

        String domain;
        String fileName;
        try {
            domain = message.getStringProperty(MessageConstants.DOMAIN);
            fileName = message.getStringProperty(MessageConstants.FILE_NAME);
            LOG.debug("received message on fsPluginSendQueue for domain={} and fileName={}", domain, fileName);
        } catch (JMSException e) {
            LOG.error("Unable to extract domainCode or fileName from JMS message");
            return;
        }

        if (StringUtils.isNotBlank(fileName)) {
            FileObject fileObject = null;
            try {
                FileSystemManager fileSystemManager = VFS.getManager();
                fileObject = fileSystemManager.resolveFile(fileName);
                if (!fileObject.exists()) {
                    LOG.warn("File does not exist: [{}] discard the JMS message", fileName);
                    return;
                }
            } catch (FileSystemException e) {
                LOG.error("Error occurred while trying to access the file to be sent: " + fileName, e);
            }

            //process the file
            LOG.debug("now send the file: {}", fileObject);
            fsSendMessagesService.processFileSafely(fileObject, domain);
        } else {
            LOG.error("Error while consuming JMS message: [{}] fileName empty.", message);
        }
    }
}
