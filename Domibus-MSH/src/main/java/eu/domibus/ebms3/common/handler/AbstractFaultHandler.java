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

package eu.domibus.ebms3.common.handler;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * Generic handler for SOAP Faults in context of ebMS3
 *
 * @author Christian Koch, Stefan Mueller
 * @since 3.0
 */
public abstract class AbstractFaultHandler implements SOAPHandler<SOAPMessageContext> {
    private static final Log LOG = LogFactory.getLog(AbstractFaultHandler.class);

    @Qualifier("jaxbContextEBMS")
    @Autowired
    protected JAXBContext jaxbContext;

    /**
     * This method extracts a ebMS3 messaging header {@link eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging} from a {@link javax.xml.soap.SOAPMessage}
     *
     * @param soapMessage
     * @return
     */
    protected Messaging extractMessaging(final SOAPMessage soapMessage) {
        Messaging messaging = null;
        try {
            messaging = ((JAXBElement<Messaging>) this.jaxbContext.createUnmarshaller().unmarshal((Node) soapMessage.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next())).getValue();
        } catch (JAXBException | SOAPException e) {
            //TODO: make nice
            AbstractFaultHandler.LOG.error("Error extracting messaging object", e);
        }

        return messaging;
    }
}
