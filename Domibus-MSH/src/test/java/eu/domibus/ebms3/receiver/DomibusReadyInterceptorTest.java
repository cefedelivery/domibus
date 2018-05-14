package eu.domibus.ebms3.receiver;

import eu.domibus.common.DomibusInitializationHelper;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class DomibusReadyInterceptorTest {

    @Injectable
    private DomibusInitializationHelper domibusInitializationHelper;

    @Tested
    private DomibusReadyInterceptor domibusReadyInterceptor;

    @Test(expected = Fault.class)
    public void isReady(@Mocked final Message message){
        new Expectations(){{
            domibusInitializationHelper.isNotReady();
            result=true;
        }};
        domibusReadyInterceptor.handleMessage(message);
    }

}