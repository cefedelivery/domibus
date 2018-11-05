
package eu.domibus.ebms3;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.interceptor.AttachmentInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;


public class SaveRequestToFileInInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final Logger LOG = LogUtils.getLogger(SaveRequestToFileInInterceptor.class);

    public SaveRequestToFileInInterceptor() {
        super(Phase.RECEIVE);
        this.addBefore(AttachmentInInterceptor.class.getName());
    }


    @Override
    public void handleMessage(Message message) throws Fault {
        InputStream is = message.getContent(InputStream.class);
        File file = new File("c:/DEV/_work/test.xml");
        try {
            FileUtils.copyInputStreamToFile(is, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
