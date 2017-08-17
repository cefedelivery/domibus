/*
 * Copyright 2015 e-CODEX Project
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 * http://ec.europa.eu/idabc/eupl5
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.domibus.ebms3.sender;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.PolicyInInterceptor;
import org.apache.cxf.ws.security.SecurityConstants;

/**
 * @author Christian Koch, Stefan Mueller
 */
public class SetSignatureAlgorithmInInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(SetSignatureAlgorithmInInterceptor.class);

    public SetSignatureAlgorithmInInterceptor() {
        super(Phase.RECEIVE);
        this.addBefore(PolicyInInterceptor.class.getName());
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {

        final Object signatureAlgorithm = message.getContextualProperty(DispatchClientDefaultProvider.ASYMMETRIC_SIG_ALGO_PROPERTY);
        if (signatureAlgorithm != null) {
            message.put(SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM, signatureAlgorithm);
        }
    }
}
