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

@Service("fsOutMessageListener")
public class FSOutMessageListener implements MessageListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSOutMessageListener.class);

    @Autowired
    private FSSendMessagesService fsSendMessagesService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void onMessage(Message message) {
        LOG.debug("received message on fsPluginOutQueue");

        String domain, fileName;
        try {
            domain = message.getStringProperty(MessageConstants.DOMAIN);
            fileName = message.getStringProperty(MessageConstants.FILE_NAME);
            LOG.debug("received message on fsPluginOutQueue for domain={} and fileName={}", domain, fileName);
        } catch (JMSException e) {
            LOG.error("Unable to extract domainCode or fileName from JMS message");
            return;
        }

        if (StringUtils.isNoneBlank(domain, fileName)) {


            FileObject fileObject  = null;
            try {
                FileSystemManager fileSystemManager = VFS.getManager();
                fileObject = fileSystemManager.resolveFile(fileName);
                if (!fileObject.exists()) {
                    LOG.error("File does not exist: {}", fileName);
                    return;
                }
            } catch (FileSystemException e) {
                LOG.error("Error occurred while trying to access the file to be sent: {}", fileName);
            }

            //send the file
            LOG.debug("now send the file: {}", fileObject);
            fsSendMessagesService.processFileSafely(fileObject, domain);
        }


    }
}
