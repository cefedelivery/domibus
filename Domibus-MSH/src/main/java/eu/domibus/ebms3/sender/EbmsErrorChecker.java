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

import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.dao.ErrorLogDao;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.logging.ErrorLogEntry;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Error;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.ObjectFactory;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.SignalMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * @author Christian Koch, Stefan Mueller
 */
@Service
public class EbmsErrorChecker {

    private static final Log LOG = LogFactory.getLog(EbmsErrorChecker.class);

    @Autowired
    @Qualifier("jaxbContextEBMS")
    JAXBContext jaxbContext;

    @Autowired
    ErrorLogDao errorLogDao;

    public EbmsErrorChecker.CheckResult check(final SOAPMessage response) throws EbMS3Exception {


        final Messaging messaging;

        try {
            messaging = this.jaxbContext.createUnmarshaller().unmarshal((Node) response.getSOAPHeader().getChildElements(ObjectFactory._Messaging_QNAME).next(), Messaging.class).getValue();
        } catch (JAXBException | SOAPException e) {
            EbmsErrorChecker.LOG.error(e.getMessage(), e);
            return EbmsErrorChecker.CheckResult.MARSHALL_ERROR;
        }

        final SignalMessage signalMessage = messaging.getSignalMessage();

        if (signalMessage.getError() == null || signalMessage.getError().size() == 0) {
            return EbmsErrorChecker.CheckResult.OK;
        }
        //TODO: piggybacking support
        for (final Error error : signalMessage.getError()) {
            if (ErrorCode.SEVERITY_FAILURE.equalsIgnoreCase(error.getSeverity())) {
                EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.findErrorCodeBy(error.getErrorCode()), error.getErrorDetail(), error.getRefToMessageInError(), null);
                e.setMshRole(MSHRole.SENDING);
                throw e;
            }

            if (ErrorCode.SEVERITY_WARNING.equalsIgnoreCase(error.getSeverity())) {
                EbMS3Exception e = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.findErrorCodeBy(error.getErrorCode()), error.getErrorDetail(), error.getRefToMessageInError(), null);

                final ErrorLogEntry errorLogEntry = new ErrorLogEntry(e);
                this.errorLogDao.create(errorLogEntry);
            }
        }

        return EbmsErrorChecker.CheckResult.WARNING;
    }

    public enum CheckResult {
        OK, WARNING, MARSHALL_ERROR
    }
}
