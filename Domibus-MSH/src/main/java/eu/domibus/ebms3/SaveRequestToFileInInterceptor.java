
package eu.domibus.ebms3;

import eu.domibus.common.services.SoapService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.AttachmentInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


@Service
public class SaveRequestToFileInInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SaveRequestToFileInInterceptor.class);

    public SaveRequestToFileInInterceptor() {
        super(Phase.RECEIVE);
        this.addBefore(AttachmentInInterceptor.class.getName());
    }

    @Autowired
    SoapService soapService;


    @Override
    public void handleMessage(Message message) throws Fault {
        String encoding = (String) message.get(Message.ENCODING);
        String ct = (String) message.get(Message.CONTENT_TYPE);

        InputStream in = message.getContent(InputStream.class);
        CachedOutputStream cos = new CachedOutputStream();

        try {
            cos.setOutputDir(new File("c:/DEV/_work/"));
            IOUtils.copy(in, cos);
            in.close();
            message.setContent(InputStream.class, cos.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOG.putMDC(Message.CONTENT_TYPE, ct);

        final String uuid = UUID.randomUUID().toString();
        String fileName = "c:/DEV/_work/test-" + uuid;
        LOG.putMDC("myfile", fileName);

        try {
            FileUtils.copyInputStreamToFile(cos.getInputStream(), new File(fileName));
            cos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        message.setContent();
//        message.getInterceptorChain().abort();
    }

    public static void main(String[] args) {
        final ContentType parse = ContentType.parse("multipart/related; type=\"application/soap+xml\"; boundary=\"uuid:8a7bd2c5-2964-4bd8-88b1-ea47bf259e5e\"; start=\"<split.root.message@cxf.apache.org>\"; start-info=\"application/soap+xml\"");
        System.out.println(parse);
    }
}
