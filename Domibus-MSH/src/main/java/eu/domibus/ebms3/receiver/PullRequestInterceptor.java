package eu.domibus.ebms3.receiver;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;

/**
 * Created by dussath on 5/29/17.
 */
public class PullRequestInterceptor extends WSS4JInInterceptor {
    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        //retrieve certificate to identify party and add it to the message.
       /* X509Certificate certificate = getSenderCertificate(message);
        String dnSubject = certificate.getSubjectDN().getName();*/

    }

}
