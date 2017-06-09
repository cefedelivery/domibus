package eu.domibus;

import eu.domibus.ebms3.receiver.SetPolicyInInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Created by draguio on 18/02/2016.
 */
public abstract class AbstractReceiveMessageIT extends AbstractIT {

    @Autowired
    SetPolicyInInterceptor setPolicyInInterceptor;

    @Autowired
    ApplicationContext applicationContext;


}
