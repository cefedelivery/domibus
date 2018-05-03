package eu.domibus.plugin.delegate;

import eu.domibus.api.util.ClassUtil;
import eu.domibus.common.MessageReceiveFailureEvent;
import eu.domibus.plugin.BackendConnector;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;

/**
 * @author Cosmin Baciu
 * @since 3.2.2
 */
@RunWith(JMockit.class)
public class DefaultBackendConnectorDelegateTest {

    @Injectable
    ClassUtil classUtil;

    @Tested
    DefaultBackendConnectorDelegate defaultBackendConnectorDelegate;

    @Test
    public void testMessageReceive(@Injectable final BackendConnector backendConnector,
                                   @Injectable final MessageReceiveFailureEvent event) throws Exception {
        defaultBackendConnectorDelegate.messageReceiveFailed(backendConnector, event);

        new Verifications() {{
            backendConnector.messageReceiveFailed(event);
        }};
    }
}
