package eu.domibus.common;

import org.apache.cxf.BusFactory;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.neethi.AssertionBuilderFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Thomas Dussart
 * @since 3.3.4
 *
 * This class ensure that the system is ready to start sending and receiving.
 * Indeed, under heavy load, there is a small glitch between the moment the spring context is ready and some cxf inner class.
 *
 */

@Component
public class System {

    private boolean contextInitiated;

    private boolean cxfInitiated;

    @PostConstruct
    public void init(){
        contextInitiated =true;
    }

    public boolean isNotReady() {
        if(contextInitiated && cxfInitiated) return false;
        final org.apache.neethi.PolicyBuilder extension = (org.apache.neethi.PolicyBuilder) BusFactory.getDefaultBus().getExtension(PolicyBuilder.class);
        final AssertionBuilderFactory assertionBuilderFactory = extension.getAssertionBuilderFactory();
        cxfInitiated=(assertionBuilderFactory!=null);
        return !(contextInitiated && cxfInitiated);
    }
}
