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

import eu.domibus.ebms3.receiver.SetPolicyOutInterceptorServer;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.PolicyInInterceptor;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * This interceptor is responsible for the exchange of parameters from a org.apache.cxf.binding.soap.SoapMessage to a javax.xml.soap.SOAPException
 *
 * @author Christian Koch, Stefan Mueller
 */
public class PropertyValueExchangeOutInterceptor extends AbstractSoapInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PropertyValueExchangeOutInterceptor.class);

    public PropertyValueExchangeOutInterceptor() {
        super(Phase.SETUP);
        this.addBefore(SetPolicyOutInterceptorServer.class.getName());
    }

    @Override
    public void handleMessage(final SoapMessage message) throws Fault {

        //message.getClass().getName()
        final SOAPMessage jaxwsMessage = message.getContent(SOAPMessage.class);
        try {
            Object property = jaxwsMessage.getProperty(MSHDispatcher.MESSAGE_TYPE_OUT);
            message.put(MSHDispatcher.MESSAGE_TYPE_OUT,property);

        } catch (final SOAPException e) {
            LOG.error("", e);
        }
    }
}


