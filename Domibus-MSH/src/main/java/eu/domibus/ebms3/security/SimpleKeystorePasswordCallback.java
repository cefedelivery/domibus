
package eu.domibus.ebms3.security;

import org.apache.wss4j.common.ext.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.util.Map;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class SimpleKeystorePasswordCallback implements CallbackHandler {

    private Map<String, String> passwordStore;

    @Override
    public void handle(final Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (final Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback) {
                final WSPasswordCallback pc = (WSPasswordCallback) callback;

                pc.setPassword(this.passwordStore.get(pc.getIdentifier()));
            }
        }
    }

    public void setPasswordStore(final Map<String, String> passwordStore) {
        this.passwordStore = passwordStore;
    }
}
