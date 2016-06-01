/*
 * Copyright 2014 e-CODEX Project
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

package eu.domibus.ebms3.common;

import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * TODO: add class description
 */
@Service
public class SOAPMessageConverterService {

    @Autowired
    @Qualifier("jaxbContextEBMS")
    private JAXBContext jaxbContext;


    public Messaging getMessaging(final SOAPMessage request) throws SOAPException, JAXBException {
        final Node messagingXml = (Node) request.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next();
        final Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller(); //Those are not thread-safe, therefore a new one is created each call
        @SuppressWarnings("unchecked") final JAXBElement<Messaging> root = (JAXBElement<Messaging>) unmarshaller.unmarshal(messagingXml);
        return root.getValue();
    }
}
