package eu.domibus.plugin.fs.queue;

import eu.domibus.messaging.MessageConstants;
import eu.domibus.plugin.fs.worker.FSSendMessagesService;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.vfs2.FileObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.Message;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class FSSendMessageListenerTest {

    @Injectable
    private FSSendMessagesService fsSendMessagesService;

    @Tested
    FSSendMessageListener fsSendMessageListener;

    @Test
    public void test_onMessage_FileExists_Success(final @Mocked Message message, final @Mocked FileObject fileObject) throws Exception {
        final String domain = "default";
        final String fileName = "/home/domibus/fs_plugin_data/MAIN/OUT/1.txt";


        new Expectations() {{
            message.getStringProperty(MessageConstants.DOMAIN);
            result = domain;

            message.getStringProperty(MessageConstants.FILE_NAME);
            result = fileName;
        }};

        //tested method
        fsSendMessageListener.onMessage(message);

        new FullVerifications(fsSendMessagesService) {{
            String domainActual;
            fsSendMessagesService.processFileSafely((FileObject) any, domainActual = withCapture());
            Assert.assertEquals(domain, domainActual);
        }};
    }
}