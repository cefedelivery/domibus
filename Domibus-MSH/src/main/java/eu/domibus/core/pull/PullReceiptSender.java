package eu.domibus.core.pull;

import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.common.exception.EbMS3Exception;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.ebms3.common.model.Error;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.ebms3.sender.MSHDispatcher;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.util.MessageUtil;
import org.apache.neethi.Policy;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.soap.SOAPMessage;
import java.util.Set;

@Component
public class PullReceiptSender {

    private static final Logger LOG = DomibusLoggerFactory.getLogger(PullReceiptSender.class);

    @Autowired
    private MSHDispatcher mshDispatcher;

    @Autowired
    protected MessageUtil messageUtil;

    @Autowired
    private DomainContextProvider domainContextProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public void sendReceipt(final SOAPMessage soapMessage, final String endpoint, final Policy policy, final LegConfiguration legConfiguration, final String pModeKey, final String messsageId, String domainCode) throws EbMS3Exception {
        domainContextProvider.setCurrentDomain(domainCode);
        LOG.trace("[sendReceipt] Message:[{}] dispatch receipt", messsageId);
        final SOAPMessage acknowledgementResult;
        try {
            acknowledgementResult = mshDispatcher.dispatch(soapMessage, endpoint, policy, legConfiguration, pModeKey);
            LOG.trace("[sendReceipt] Message:[{}] receipt result", messsageId);
            handleDispatchReceiptResult(acknowledgementResult);
        } catch (EbMS3Exception e) {
            LOG.error("Error dispatching the pull receipt for message:[{}]", messsageId, e);
            throw e;
        } finally {
            LOG.trace("[sendReceipt] ~~~ finnaly the end ~~~");
        }
    }

    private void handleDispatchReceiptResult(SOAPMessage acknowledgementResult) throws EbMS3Exception {
        if (acknowledgementResult == null) {
            LOG.debug("acknowledgementResult is null, as expected. No errors were reported");
            return;
        }
        Messaging errorMessage = messageUtil.getMessage(acknowledgementResult);
        if (errorMessage == null || errorMessage.getSignalMessage() == null) {
            LOG.debug("acknowledgementResult is not null, but it does not contain a SignalMessage with the reported errors. ");
            return;
        }
        Set<Error> errors = errorMessage.getSignalMessage().getError();
        if (errors != null && !errors.isEmpty()) {
            Error error = errors.iterator().next();
            LOG.error("An error occured when sending receipt:error code:[{}], description:[{}]:[{}]", error.getErrorCode(), error.getShortDescription(), error.getErrorDetail());
            EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.findErrorCodeBy(error.getErrorCode()), error.getErrorDetail(), error.getRefToMessageInError(), null);
            ebMS3Ex.setMshRole(MSHRole.RECEIVING);
            throw ebMS3Ex;

        }
    }
}
