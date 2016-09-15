package eu.domibus;

import eu.domibus.ebms3.receiver.SetPolicyInInterceptor;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.bus.managers.PhaseManagerImpl;
import org.apache.cxf.interceptor.InterceptorChain;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by draguio on 18/02/2016.
 */
public abstract class AbstractReceiveMessageIT extends AbstractIT {

    @Autowired
    SetPolicyInInterceptor setPolicyInInterceptor;

    @Autowired
    ApplicationContext applicationContext;

    protected SOAPMessage createSOAPMessage(String dataset) throws SOAPException, IOException {
        InputStream is = new FileInputStream(new File("target/test-classes/dataset/as4/" + dataset).getAbsolutePath());

        SoapMessage sm = new SoapMessage(new MessageImpl());
        sm.setContent(InputStream.class, is);
        InterceptorChain ic = new PhaseInterceptorChain((new PhaseManagerImpl()).getOutPhases());
        sm.setInterceptorChain(ic);

        setPolicyInInterceptor.handleMessage(sm);

        return sm.getContent(SOAPMessage.class);
    }

}
