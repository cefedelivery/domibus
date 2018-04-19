package eu.domibus.common;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.neethi.AssertionBuilderFactory;
import org.hibernate.jdbc.Expectations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Thomas Dussart
 * @see org.apache.cxf.ws.policy.PolicyBuilderImpl
 * @since 3.3.4
 * <p>
 * This class ensure that the system is ready to start sending and receiving.
 * Indeed in PolicyBuilderImpl you can see that the constructor is instantiating the factory to null, then call
 * the setBus method which will set the factory (protected member of superclass). If messages are received between
 * the two operations, a nullpointer is triggered. This class ensure that the system is properly initialize before sending
 * or receiving messages.
 */

@Component
public class DomibusInitializationHelper {

    private boolean cxfInitiated;

    @Autowired
    private Bus bus;

    public boolean isNotReady() {
        if (cxfInitiated) return false;
        final org.apache.neethi.PolicyBuilder extension = (org.apache.neethi.PolicyBuilder) bus.getExtension(PolicyBuilder.class);
        if (extension == null) {
            return true;
        }
        final AssertionBuilderFactory assertionBuilderFactory = extension.getAssertionBuilderFactory();
        cxfInitiated = (assertionBuilderFactory == null);
        return cxfInitiated;
    }
}
